// sensorStompService.js — Sensor WebSocket Service
import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';

class SensorStompService {
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
                    if (sid && tail) return {sid, url};
                } catch (_) {
                }
            }
            return null;
        };

        while (Date.now() - start < 4000) {
            const got = pick();
            if (got) {
                this.sessionId = got.sid;
                return got.sid;
            }
            await new Promise((r) => setTimeout(r, 100));
        }
        throw new Error('SockJS sessionId not found (timeout)');
    }

    // 연결
    connect(token, userType = 'admin') {
        return new Promise((resolve, reject) => {
            if (this.client?.active || this.connected) return resolve();

            this.token = token;
            this.userType = userType;

            const wsUrl = process.env.REACT_APP_SENSOR_WS_URL || 'http://localhost:8083/sensor/ws';
            if (!token) console.warn('[SENSOR WS] token is empty!');

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
                reconnectDelay: 0, // 필요 시 자동재연결 사용
            });

            // 연결 성공
            this.client.onConnect = async (frame) => {
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
                console.warn('🔌 Sensor WebSocket closed:', evt?.reason || evt);
                this.connected = false;
                this.emit('disconnected');
            };

            this.client.activate();
        });
    }

    // 구독 설정
    async setupSubscriptions() {
        // 관리자만 센서 데이터 수신
        this.subscribe('/sensor/topic/sensors/admin', (message) => this.handleSensorMessage(message));
    }

    // 메시지 파싱
    handleSensorMessage(message) {
        try {
            const body = typeof message?.body === 'string' ? message.body : '';

            // 센서 데이터 메시지 파싱 (관리자용만)
            const sensorRegex = /\[센서 업데이트\] 작업자 ID: (\d+), 위치: \(([\d.-]+), ([\d.-]+)\), 심박수: ([\d.]+), 걸음수: (\d+)/;

            let data;
            const adminMatch = body.match(sensorRegex);

            if (adminMatch) {
                data = {
                    type: 'sensor_update',
                    workerId: parseInt(adminMatch[1]),
                    latitude: parseFloat(adminMatch[2]),
                    longitude: parseFloat(adminMatch[3]),
                    heartRate: parseFloat(adminMatch[4]),
                    steps: parseInt(adminMatch[5]),
                    timestamp: new Date().toISOString(),
                };
                this.emit('sensor-update', data);
            } else {
                // 기타 센서 관련 메시지
                data = {
                    type: 'general_sensor',
                    message: body || '센서 메시지',
                    timestamp: new Date().toISOString(),
                };
                this.emit('sensor-message', data);
            }

            // 모든 센서 메시지를 sensor 이벤트로도 발행
            this.emit('sensor', data);
        } catch (e) {
            console.error('센서 메시지 처리 에러:', e, '원본:', message?.body);
            this.emit('sensor-error', {
                type: 'error',
                message: '센서 데이터 처리 중 오류가 발생했습니다',
                timestamp: new Date().toISOString(),
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
        this.client.publish({destination, headers, body: JSON.stringify(body || {})});
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

const sensorStompService = new SensorStompService();
export default sensorStompService;