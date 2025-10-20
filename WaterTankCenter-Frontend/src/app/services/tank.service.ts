import { Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import { Client, Stomp } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { WaterTank } from '../models/water-tank.model';
import { HouseDto } from '../models/house.mode';
@Injectable({
  providedIn: 'root',
})
export class TankService {
  private stompClient: any;

  private messageWaterTank: BehaviorSubject<WaterTank | null> =
    new BehaviorSubject<WaterTank | null>(null);

  private messageHouseDto: BehaviorSubject<HouseDto | null> =
    new BehaviorSubject<HouseDto | null>(null);

  initWebSocketConnection() {
    const url = '//localhost:8080/ws-tank';
    const socket = new SockJS(url);
    this.stompClient = Stomp.over(socket);
  }

  susbcribeClient() {
    this.stompClient.connect({}, () => {

      this.stompClient.subscribe('/topic/tank-status', (message: any) => {
        const content = JSON.parse(message.body);
        this.messageWaterTank.next(content);
      });

      this.stompClient.subscribe('/topic/house-water-tank', (message: any) => {
        const content = JSON.parse(message.body);
        this.messageHouseDto.next(content);
      });
    });
  }

  getWaterTankCentral() {
    return this.messageWaterTank.asObservable();
  }

  getWaterTankHouse(){
    return this.messageHouseDto.asObservable();
  }
}
