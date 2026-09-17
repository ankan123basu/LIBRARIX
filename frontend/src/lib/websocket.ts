import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Notification } from './types';

let stompClient: Client | null = null;

export function connectWebSocket(userId: string, onNotificationReceived: (notification: Notification) => void) {
  if (stompClient && stompClient.connected) {
    return stompClient;
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS(process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8085/ws-notify'),
    reconnectDelay: 5000,
    debug: (str) => console.log('[STOMP]', str),
    onConnect: () => {
      console.log('[STOMP] Connected to WebSocket broker!');
      const destination = `/topic/user/${userId}`;
      stompClient?.subscribe(destination, (message) => {
        if (message.body) {
          try {
            const notification: Notification = JSON.parse(message.body);
            onNotificationReceived(notification);
          } catch (e) {
            console.error('[STOMP] Parse error:', e);
          }
        }
      });
    },
    onStompError: (frame) => {
      console.error('[STOMP] Error frame:', frame);
    },
  });

  stompClient.activate();
  return stompClient;
}

export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
}
