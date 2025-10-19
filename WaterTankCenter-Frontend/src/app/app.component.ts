import { Component, OnInit, OnDestroy } from '@angular/core';
import { ClarityModule } from '@clr/angular';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './components/header/header.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { TankService } from './services/tank.service';
import { WaterTankComponent } from './components/water-tank/water-tank.component';
import { WaterLevelGraphComponent } from "./components/water-level-graph/water-level-graph.component";

// Interfaz para consumidor de agua
interface WaterConsumer {
  houseCode: string;
  consumptionTime: number; // en minutos
  cubicMeters: number;
  isConsuming: boolean;
}

// Interfaz para datos de gráficas
interface ChartDataPoint {
  name: string;
  value: number;
}

@Component({
  selector: 'app-root',
  imports: [
    ClarityModule,
    CommonModule,
    WaterTankComponent,
    HeaderComponent,
    NgxChartsModule,
    WaterLevelGraphComponent
],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema de Monitoreo de Tanque de Agua';

  // Propiedades del tanque
  waterLevel: number = 0; // Nivel de agua en porcentaje (0-100)
  tankCapacity: number = 1000; // Capacidad total en litros
  currentVolume: number = 0; // Volumen actual en litros
  isFillingAnimation: boolean = false;

  // Propiedades de usuarios consumiendo
  consumers: WaterConsumer[] = [];
  totalActiveConsumers: number = 0;

  constructor(private wsService: TankService) {}

  // Datos para gráficas
  waterLevelHistory: any[] = [
    {
      name: 'Nivel de Agua',
      series: [],
    },
  ];

  consumptionRateHistory: any[] = [
    {
      name: 'Consumo Total',
      series: [],
    },
  ];

  activeConsumersHistory: any[] = [
    {
      name: 'Consumidores Activos',
      series: [],
    },
  ];

  // Configuración de gráficas
  view: [number, number] = [700, 300];
  legend: boolean = true;
  showLabels: boolean = true;
  animations: boolean = true;
  xAxis: boolean = true;
  yAxis: boolean = true;
  showYAxisLabel: boolean = true;
  showXAxisLabel: boolean = true;
  xAxisLabel: string = 'Tiempo';
  timeline: boolean = true;

  colorScheme: any = {
    domain: ['#0072a3', '#60b515', '#ff6b6b', '#ffd93d'],
  };

  consumptionColorScheme: any = {
    domain: ['#60b515'],
  };

  activeConsumersColorScheme: any = {
    domain: ['#ffd93d'],
  };

  // WebSocket (por ahora simulado)
  private intervalId: any;
  private consumersIntervalId: any;
  private chartUpdateIntervalId: any;
  private dataPointCounter: number = 0;

  ngOnInit() {
    // Simular datos del WebSocket con valores aleatorios
    // Más adelante esto se conectará a un WebSocket real
    this.simulateWebSocketData();
    this.simulateConsumersData();
    this.initializeChartData();
    this.startChartUpdates();

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
    if (this.chartUpdateIntervalId) {
      clearInterval(this.chartUpdateIntervalId);
    }
  }

  private simulateWebSocketData() {
    // Simular cambios en el nivel de agua cada 3 segundos
    this.intervalId = setInterval(() => {
      const previousLevel = this.waterLevel;
      // Generar un nivel aleatorio entre 0 y 100
      this.waterLevel = Math.floor(Math.random() * 101);
      this.currentVolume = Math.floor(
        (this.waterLevel / 100) * this.tankCapacity,
      );

      // Detectar si está llenando o vaciando
      this.isFillingAnimation = this.waterLevel > previousLevel;
    }, 3000);
  }

  // Método para obtener el color según el nivel
  getWaterColor(): string {
    if (this.waterLevel < 20) return '#ff6b6b'; // Rojo - Bajo
    if (this.waterLevel < 50) return '#ffd93d'; // Amarillo - Medio
    return '#6bcf7f'; // Verde - Alto
  }

  // Método para obtener el estado del tanque
  getTankStatus(): string {
    if (this.waterLevel < 20) return 'Nivel Crítico';
    if (this.waterLevel < 50) return 'Nivel Medio';
    return 'Nivel Óptimo';
  }

  // Método para obtener la clase de alerta
  getAlertClass(): string {
    if (this.waterLevel < 20) return 'alert-danger';
    if (this.waterLevel < 50) return 'alert-warning';
    return 'alert-success';
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

  // Inicializar datos de gráficas
  private initializeChartData() {
    const now = new Date();

    // Inicializar con algunos puntos históricos
    for (let i = 10; i >= 0; i--) {
      const time = new Date(now.getTime() - i * 3000);
      const timeLabel = time.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });

      this.waterLevelHistory[0].series.push({
        name: timeLabel,
        value: Math.floor(Math.random() * 101),
      });

      this.consumptionRateHistory[0].series.push({
        name: timeLabel,
        value: parseFloat((Math.random() * 5).toFixed(2)),
      });

      this.activeConsumersHistory[0].series.push({
        name: timeLabel,
        value: Math.floor(Math.random() * 8) + 1,
      });
    }

    this.dataPointCounter = 11;
  }

  // Actualizar datos de gráficas
  private startChartUpdates() {
    this.chartUpdateIntervalId = setInterval(() => {
      this.updateChartData();
    }, 3000); // Actualizar cada 3 segundos
  }

  private updateChartData() {
    const now = new Date();
    const timeLabel = now.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });

    // Actualizar gráfica de nivel de agua
    this.waterLevelHistory[0].series.push({
      name: timeLabel,
      value: this.waterLevel,
    });

    // Mantener solo los últimos 20 puntos
    if (this.waterLevelHistory[0].series.length > 20) {
      this.waterLevelHistory[0].series.shift();
    }

    // Actualizar gráfica de consumo total (en m³)
    const totalConsumption = this.consumers.reduce(
      (sum, c) => sum + (c.isConsuming ? 0.05 : 0),
      0,
    );
    this.consumptionRateHistory[0].series.push({
      name: timeLabel,
      value: parseFloat(totalConsumption.toFixed(2)),
    });

    if (this.consumptionRateHistory[0].series.length > 20) {
      this.consumptionRateHistory[0].series.shift();
    }

    // Actualizar gráfica de consumidores activos
    this.activeConsumersHistory[0].series.push({
      name: timeLabel,
      value: this.totalActiveConsumers,
    });

    if (this.activeConsumersHistory[0].series.length > 20) {
      this.activeConsumersHistory[0].series.shift();
    }

    // Forzar actualización de las gráficas
    this.waterLevelHistory = [...this.waterLevelHistory];
    this.consumptionRateHistory = [...this.consumptionRateHistory];
    this.activeConsumersHistory = [...this.activeConsumersHistory];

    this.dataPointCounter++;
  }

  // Formatear tooltip de las gráficas
  formatTooltip(data: any): string {
    return `${data.value}`;
  }
}
