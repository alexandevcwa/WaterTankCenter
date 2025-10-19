import { Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import { Client, Stomp } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { WaterTank } from '../models/water-tank.model';
@Injectable({
  providedIn: 'root',
})
export class TankService {

  private stompClient: any;
  private messageSubject: BehaviorSubject<WaterTank | null> = new BehaviorSubject<WaterTank | null>(null);


  initWebSocketConnection() {
    const url = '//localhost:8080/ws-tank';
    const socket = new SockJS(url);
    this.stompClient = Stomp.over(socket);
  }

  susbcribeClient() {
    this.stompClient.connect({}, () => {
      this.stompClient.subscribe('/topic/tank-status', (message: any) => {
        const content = JSON.parse(message.body);
        console.log(content);
        this.messageSubject.next(content);
      });
    });
  }

  getTankStatus(){
    return this.messageSubject.asObservable();
  }
}
