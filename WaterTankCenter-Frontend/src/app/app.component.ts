import { Component, OnInit, OnDestroy } from '@angular/core';
import { ClarityModule } from '@clr/angular';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './components/header/header.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { TankService } from './services/tank.service';
import { WaterTankComponent } from './components/water-tank/water-tank.component';
import { WaterLevelGraphComponent } from './components/water-level-graph/water-level-graph.component';
import { ConsumersComponent } from './components/consumers/consumers.component';
import { HouseFormComponent } from "./components/house-form/house-form.component";

@Component({
  selector: 'app-root',
  imports: [
    ClarityModule,
    CommonModule,
    WaterTankComponent,
    HeaderComponent,
    NgxChartsModule,
    WaterLevelGraphComponent,
    ConsumersComponent,
    HouseFormComponent
],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, OnDestroy {

  title = 'Sistema de Monitoreo de Tanque de Agua';
  opened = false;

  constructor(private wsService: TankService) {}

  openSideBar(){
    this.opened = true;
  }

  closeSideBar(){
    this.opened = false;
  }


  ngOnInit() {
    try {
      this.wsService.initWebSocketConnection();
      this.wsService.susbcribeClient();
    } catch (error) {
      console.log(error);
    }
  }

  ngOnDestroy() {}
}
