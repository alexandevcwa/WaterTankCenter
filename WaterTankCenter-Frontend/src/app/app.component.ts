import { Component, OnInit, OnDestroy } from '@angular/core';
import { ClarityModule } from '@clr/angular';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './components/header/header.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { TankService } from './services/tank.service';
import { WaterTankComponent } from './components/water-tank/water-tank.component';
import { WaterLevelGraphComponent } from "./components/water-level-graph/water-level-graph.component";
import { ConsumersComponent } from "./components/consumers/consumers.component";

// Interfaz para consumidor de agua
interface WaterConsumer {
  houseCode: string;
  consumptionTime: number; // en minutos
  cubicMeters: number;
  isConsuming: boolean;
}

@Component({
  selector: 'app-root',
  imports: [
    ClarityModule,
    CommonModule,
    WaterTankComponent,
    HeaderComponent,
    NgxChartsModule,
    WaterLevelGraphComponent,
    ConsumersComponent
],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema de Monitoreo de Tanque de Agua';


  // Propiedades de usuarios consumiendo
  consumers: WaterConsumer[] = [];
  totalActiveConsumers: number = 0;

  constructor(private wsService: TankService) {}

  // WebSocket (por ahora simulado)
  private intervalId: any;
  private consumersIntervalId: any;


  ngOnInit() {
    this.simulateConsumersData();

    try {
      this.wsService.initWebSocketConnection();
      this.wsService.susbcribeClient();
    } catch (error) {
      console.log(error);
    }
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    if (this.consumersIntervalId) {
      clearInterval(this.consumersIntervalId);
    }
  }


  // Simular datos de consumidores (más adelante será WebSocket real)
  private simulateConsumersData() {
    // Generar datos iniciales
    this.generateRandomConsumers();

    // Actualizar datos cada 5 segundos
    this.consumersIntervalId = setInterval(() => {
      this.updateConsumersData();
    }, 5000);
  }

  private generateRandomConsumers() {
    const houseCodes = [
      'A-101',
      'A-102',
      'B-201',
      'B-202',
      'C-301',
      'C-302',
      'D-401',
      'D-402',
    ];
    const numConsumers = Math.floor(Math.random() * 6) + 3; // Entre 3 y 8 consumidores

    this.consumers = [];
    for (let i = 0; i < numConsumers; i++) {
      const houseCode =
        houseCodes[Math.floor(Math.random() * houseCodes.length)];
      // Evitar duplicados
      if (this.consumers.some((c) => c.houseCode === houseCode)) continue;

      this.consumers.push({
        houseCode: houseCode,
        consumptionTime: Math.floor(Math.random() * 120) + 10, // Entre 10 y 130 minutos
        cubicMeters: parseFloat((Math.random() * 2.5 + 0.1).toFixed(3)), // Entre 0.1 y 2.6 m³
        isConsuming: Math.random() > 0.3, // 70% activos
      });
    }

    this.updateActiveConsumersCount();
  }

  private updateConsumersData() {
    // Actualizar consumidores existentes
    this.consumers = this.consumers.map((consumer) => {
      if (consumer.isConsuming) {
        return {
          ...consumer,
          consumptionTime:
            consumer.consumptionTime + Math.floor(Math.random() * 3) + 1,
          cubicMeters: parseFloat(
            (consumer.cubicMeters + Math.random() * 0.05).toFixed(3),
          ),
          isConsuming: Math.random() > 0.1, // 90% probabilidad de seguir consumiendo
        };
      }
      return {
        ...consumer,
        isConsuming: Math.random() > 0.7, // 30% probabilidad de volver a consumir
      };
    });

    // Aleatoriamente agregar o quitar consumidores
    if (Math.random() > 0.7 && this.consumers.length < 8) {
      const houseCodes = [
        'A-101',
        'A-102',
        'B-201',
        'B-202',
        'C-301',
        'C-302',
        'D-401',
        'D-402',
      ];
      const availableCodes = houseCodes.filter(
        (code) => !this.consumers.some((c) => c.houseCode === code),
      );

      if (availableCodes.length > 0) {
        const newCode =
          availableCodes[Math.floor(Math.random() * availableCodes.length)];
        this.consumers.push({
          houseCode: newCode,
          consumptionTime: 1,
          cubicMeters: 0.001,
          isConsuming: true,
        });
      }
    } else if (Math.random() > 0.8 && this.consumers.length > 2) {
      // Remover un consumidor aleatorio
      this.consumers.splice(
        Math.floor(Math.random() * this.consumers.length),
        1,
      );
    }

    this.updateActiveConsumersCount();
  }

  private updateActiveConsumersCount() {
    this.totalActiveConsumers = this.consumers.filter(
      (c) => c.isConsuming,
    ).length;
  }

  // Formatear tiempo de consumo
  formatConsumptionTime(minutes: number): string {
    if (minutes < 60) {
      return `${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}min`;
  }

  // Obtener clase de badge para estado de consumo
  getConsumptionBadgeClass(isConsuming: boolean): string {
    return isConsuming ? 'badge-success' : 'badge-secondary';
  }

  // Calcular el total de metros cúbicos consumidos
  getTotalCubicMeters(): string {
    const total = this.consumers.reduce(
      (sum, consumer) => sum + consumer.cubicMeters,
      0,
    );
    return total.toFixed(3);
  }
}
