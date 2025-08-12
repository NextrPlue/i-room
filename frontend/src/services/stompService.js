// stompService.js — @stomp/stompjs Client 버전 (세션ID 기반 + 헤더 포함)
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class StompService {
    constructor() {
        this.client = null;           // @stomp/stompjs Client
        this.sock = null;             // SockJS 인스턴스 (세션ID 추출용)
        this.connected = false;

        this.subscriptions = {};
        this.listeners = {};

        this.token = null;
        this.userType = null;
        this.sessionId = null;
    }

    // SockJS 세션ID 추출 (최대 4초 폴링)
    async resolveSockJsSessionId() {
        const start = Date.now();

        const pick = () => {
            const s = this.sock;
            const urls = [
                s?._transport?.url,
                s?._transport?.transport?.url, // 일부 전송모드에서 이 경로에 있음
            ].filter(Boolean);

            for (const url of urls) {
                try {
                    const parts = url.split('/');
                    const sid = parts[parts.length - 2]; // 끝-1이 세션ID
                    const tail = parts[parts.length - 1];
                    if (sid && tail) return { sid, url };
                } catch (_) {}
            }
            return null;
        };

        while (Date.now() - start < 4000) {
            const got = pick();
            if (got) {
                console.log('[WS] SockJS sessionId:', got.sid, 'from', got.url);
                this.sessionId = got.sid;
                return got.sid;
            }
            await new Promise((r) => setTimeout(r, 100));
        }
        throw new Error('SockJS sessionId not found (timeout)');
    }

    // 연결
    connect(token, userType = 'worker') {
        return new Promise((resolve, reject) => {
            if (this.client?.active || this.connected) return resolve();

            this.token = token;
            this.userType = userType;

            const wsUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8084/ws';
            console.log('[WS] connecting to', wsUrl);
            if (!token) console.warn('[WS] token is empty!');

            // SockJS 인스턴스를 직접 만들어서 보관(세션ID 추출용)
            const socket = new SockJS(wsUrl);
            this.sock = socket;

            // Client 생성
            this.client = new Client({
                webSocketFactory: () => socket,
                connectHeaders: {
                    Authorization: `Bearer ${token}`,
                    authorization: `Bearer ${token}`,
                    'auth-token': token,
                    token: token,
                },
                debug: (s) => console.log('[STOMP]', s),
                reconnectDelay: 0, // 필요 시 자동재연결 사용
            });

            // 연결 성공
            this.client.onConnect = async (frame) => {
                console.log('✅ STOMP Connected:', frame);
                try {
                    await this.resolveSockJsSessionId();
                    this.connected = true;
                    await this.setupSubscriptions();
                    this.emit('connected');
                    resolve();
                } catch (e) {
                    console.error('onConnect/setup error:', e);
                    this.emit('error', e);
                    reject(e);
                }
            };

            // 브로커에서 보낸 STOMP ERROR 프레임
            this.client.onStompError = (frame) => {
                const msg = frame?.headers?.message;
                const body = frame?.body;
                console.error('[STOMP][ERROR] message:', msg);
                console.error('[STOMP][ERROR] body:', body);
                this.connected = false;
                this.emit('error', frame);
                reject(frame);
            };

            // 소켓이 닫힘
            this.client.onWebSocketClose = (evt) => {
                console.warn('🔌 WebSocket closed:', evt?.reason || evt);
                this.connected = false;
                this.emit('disconnected');
            };

            this.client.activate();
        });
    }

    // 구독 설정
    async setupSubscriptions() {
        if (this.userType === 'admin') {
            this.subscribe('/topic/alarms/admin', (message) => this.handleAlarmMessage(message));
            console.log('🔴 관리자 모드 구독: /topic/alarms/admin');
        } else {
            if (!this.sessionId) throw new Error('No sessionId; cannot subscribe worker queue.');
            const destination = `/queue/alarms-${this.sessionId}`;
            this.subscribe(destination, (message) => {
                console.log('🟢 [worker queue]', destination, 'msg:', message?.body);
                this.handleAlarmMessage(message);
            });
            console.log('🟢 근로자 모드 구독:', destination);
        }
    }

    // 메시지 파싱
    handleAlarmMessage(message) {
        try {
            const body = typeof message?.body === 'string' ? message.body : '';
            console.log('📨 원본 메시지:', body);

            const regex = /\[([^\]]+)\]\s*(.+)/;
            const match = body.match(regex);

            let data;
            if (match) {
                const incidentType = match[1];
                const description = match[2];
                const workerIdMatch = description.match(/작업자 ID: (\d+)/);
                const workerId = workerIdMatch ? workerIdMatch[1] : null;
                const imageUrlMatch = body.match(/\((https?:\/\/[^\)]+)\)/);
                const imageUrl = imageUrlMatch ? imageUrlMatch[1] : null;

                data = {
                    incidentType,
                    incidentDescription: description
                        .replace(/\s*\(작업자 ID: \d+\)/, '')
                        .replace(/\s*\(https?:\/\/[^\)]+\)/, '')
                        .trim(),
                    workerId,
                    workerImageUrl: imageUrl,
                    occurredAt: new Date().toISOString(),
                };
            } else {
                let type = 'PPE_VIOLATION';
                if (body.includes('위험구역') || body.includes('DANGER_ZONE')) type = 'DANGER_ZONE';
                else if (body.includes('건강') || body.includes('HEALTH_RISK')) type = 'HEALTH_RISK';

                data = {
                    incidentType: type,
                    incidentDescription: body || '메시지 본문 없음',
                    workerId: null,
                    occurredAt: new Date().toISOString(),
                };
            }

            console.log('📨 파싱 데이터:', data);

            switch (data.incidentType) {
                case 'PPE_VIOLATION':
                    this.emit('safety-gear-alert', data);
                    break;
                case 'DANGER_ZONE':
                    this.emit('danger-zone-alert', data);
                    break;
                case 'HEALTH_RISK':
                    this.emit('health-risk-alert', data);
                    break;
                default:
                    this.emit('unknown-alert', data);
            }
            this.emit('alarm', data);
        } catch (e) {
            console.error('메시지 처리 에러:', e, '원본:', message?.body);
            this.emit('safety-gear-alert', {
                incidentType: 'PPE_VIOLATION',
                incidentDescription: '알람이 발생했습니다',
                workerId: null,
                occurredAt: new Date().toISOString(),
            });
        }
    }

    // 구독 (SUBSCRIBE 프레임에도 토큰 헤더 포함)
    subscribe(destination, callback) {
        if (!this.client || !this.connected) {
            throw new Error('There is no underlying STOMP connection');
        }
        const headers = {
            Authorization: `Bearer ${this.token}`,
            authorization: `Bearer ${this.token}`,
            'auth-token': this.token,
            token: this.token,
        };
        const sub = this.client.subscribe(destination, callback, headers);
        this.subscriptions[destination] = sub;
        return sub;
    }

    // SEND에도 토큰 헤더 포함
    send(destination, body) {
        if (!this.client || !this.connected) {
            console.error('Not connected');
            return;
        }
        const headers = {
            Authorization: `Bearer ${this.token}`,
            authorization: `Bearer ${this.token}`,
            'auth-token': this.token,
            token: this.token,
        };
        this.client.publish({ destination, headers, body: JSON.stringify(body || {}) });
    }

    // 연결 해제
    disconnect() {
        if (this.client) {
            Object.values(this.subscriptions).forEach((s) => s.unsubscribe());
            this.subscriptions = {};
            this.client.deactivate();
            this.connected = false;
            this.emit('disconnected');
        }
    }

    // 이벤트 버스
    on(event, cb) {
        (this.listeners[event] ||= []).push(cb);
    }
    off(event, cb) {
        if (this.listeners[event]) {
            this.listeners[event] = this.listeners[event].filter((f) => f !== cb);
        }
    }
    emit(event, data) {
        (this.listeners[event] || []).forEach((cb) => cb(data));
    }

    isConnected() {
        return this.connected;
    }
}

const stompService = new StompService();
export default stompService;
