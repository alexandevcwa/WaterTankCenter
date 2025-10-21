import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { TankService } from '../../services/tank.service';
import { WaterTank } from '../../models/water-tank.model';

@Component({
  selector: 'app-water-tank',
  imports: [CommonModule],
  templateUrl: './water-tank.component.html',
  styleUrl: './water-tank.component.css',
})
export class WaterTankComponent implements OnInit, OnDestroy {

  isFillingAnimation = false;
  waterTank!: WaterTank;
  waterLevel = 0;

  constructor(private tankService: TankService) {}

  ngOnDestroy(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  ngOnInit(): void {
    this.listenerTankStatus();
    const temp: WaterTank = {
      id: 0,
      maximumVolume: 0,
      saveVolume: 0,
      minimumVolume: 0,
      currentVolume: 0,
      radio: 0,
      height: 0,
      currentPercentage: 0.0,
      filling: true,
      diameter: 0.0
    };
    this.waterTank = temp;
  }

  listenerTankStatus() {
    this.tankService.getWaterTankCentral().subscribe((message: WaterTank | null) => {
      if (!message) return;

      if (this.waterTank.currentVolume === message.currentVolume) {
        this.isFillingAnimation = false;
        return;
      }

      this.waterTank = message;
      this.waterTank.maximumVolume = Math.floor(this.waterTank.maximumVolume);

      const level = (message.currentVolume / message.maximumVolume) * 100;
      this.waterLevel = Math.floor(level);
    });
  }

  private intervalId: any;

  getWaterColor(): string {
    if (this.waterLevel < 20) return '#ff6b6b'; // Rojo - Bajo
    if (this.waterLevel < 50) return '#ffd93d'; // Amarillo - Medio
    return '#6bcf7f'; // Verde - Alto
  }

  // Método para obtener la clase de alerta
  getAlertClass(): string {
    if (this.waterLevel < 20) return 'alert-danger';
    if (this.waterLevel < 50) return 'alert-warning';
    return 'alert-success';
  }

  // Método para obtener el estado del tanque
  getTankStatus(): string {
    if (this.waterLevel < 20) return 'Nivel Crítico';
    if (this.waterLevel < 50) return 'Nivel Medio';
    return 'Nivel Óptimo';
  }
}
