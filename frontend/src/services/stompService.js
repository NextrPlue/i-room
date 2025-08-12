import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class StompService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = {};
        this.listeners = {};
        this.token = null;
        this.userType = null;
        this.sessionId = null;
    }

    // 연결
    connect(token, userType = 'worker') {
        return new Promise((resolve, reject) => {
            if (this.connected) {
                console.log('Already connected');
                return resolve();
            }

            this.token = token;
            this.userType = userType;

            // SockJS 연결
            const socket = new SockJS(process.env.REACT_APP_WS_URL || 'http://localhost:8084/ws');
            this.stompClient = Stomp.over(socket);

            // 디버그 모드 (개발시에만)
            if (process.env.NODE_ENV === 'development') {
                this.stompClient.debug = (str) => {
                    console.log('STOMP: ' + str);
                };
            }

            const headers = {
                'Authorization': `Bearer ${token}`
            };

            this.stompClient.connect(
                headers,
                (frame) => {
                    console.log('✅ STOMP Connected:', frame);
                    this.connected = true;

                    // 세션 ID 추출
                    try {
                        this.sessionId = this.stompClient.ws._transport.url.split('/')[5];
                        console.log('Session ID:', this.sessionId);
                    } catch (e) {
                        console.warn('Could not extract session ID:', e);
                    }

                    // 구독 설정
                    this.setupSubscriptions();

                    this.emit('connected');
                    resolve();
                },
                (error) => {
                    console.error('❌ STOMP Connection error:', error);
                    this.connected = false;
                    this.emit('error', error);
                    reject(error);
                }
            );
        });
    }

    // 구독 설정
    setupSubscriptions() {
        if (this.userType === 'admin') {
            // 관리자: 모든 알람 수신
            this.subscribe('/topic/alarms/admin', (message) => {
                this.handleAlarmMessage(message);
            });
            console.log('🔴 관리자 모드로 구독');
        } else {
            // 근로자: 개인 알람만 수신
            const destination = `/queue/alarms-${this.sessionId}`;
            this.subscribe(destination, (message) => {
                this.handleAlarmMessage(message);
            });
            console.log('🟢 근로자 모드로 구독:', destination);
        }
    }

    // 알람 메시지 처리
    handleAlarmMessage(message) {
        try {
            const data = JSON.parse(message.body);
            console.log('📨 알람 수신:', data);

            // 알람 타입별 이벤트 발생
            switch(data.incidentType) {
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

            // 전체 알람 이벤트도 발생
            this.emit('alarm', data);

        } catch (error) {
            console.error('메시지 파싱 에러:', error);
        }
    }

    // 구독
    subscribe(destination, callback) {
        if (!this.stompClient || !this.connected) {
            console.error('Not connected');
            return;
        }

        const subscription = this.stompClient.subscribe(destination, callback);
        this.subscriptions[destination] = subscription;
        return subscription;
    }

    // 구독 해제
    unsubscribe(destination) {
        if (this.subscriptions[destination]) {
            this.subscriptions[destination].unsubscribe();
            delete this.subscriptions[destination];
        }
    }

    // 메시지 전송 (필요시)
    send(destination, body) {
        if (!this.stompClient || !this.connected) {
            console.error('Not connected');
            return;
        }

        this.stompClient.send(destination, {}, JSON.stringify(body));
    }

    // 연결 해제
    disconnect() {
        if (this.stompClient) {
            // 모든 구독 해제
            Object.values(this.subscriptions).forEach(sub => sub.unsubscribe());
            this.subscriptions = {};

            this.stompClient.disconnect(() => {
                console.log('Disconnected');
                this.connected = false;
                this.emit('disconnected');
            });
        }
    }

    // 이벤트 리스너 등록
    on(event, callback) {
        if (!this.listeners[event]) {
            this.listeners[event] = [];
        }
        this.listeners[event].push(callback);
    }

    // 이벤트 리스너 제거
    off(event, callback) {
        if (this.listeners[event]) {
            this.listeners[event] = this.listeners[event].filter(cb => cb !== callback);
        }
    }

    // 이벤트 발생
    emit(event, data) {
        if (this.listeners[event]) {
            this.listeners[event].forEach(callback => callback(data));
        }
    }

    // 연결 상태 확인
    isConnected() {
        return this.connected;
    }
}

// 싱글톤 인스턴스
const stompService = new StompService();

export default stompService;