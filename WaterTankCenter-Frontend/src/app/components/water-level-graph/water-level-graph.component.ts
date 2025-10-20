import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { LineChartModule } from "@swimlane/ngx-charts";
import { ClrIconModule } from "@clr/angular";
import { TankService } from '../../services/tank.service';
import { WaterTank } from '../../models/water-tank.model';

@Component({
  selector: 'app-water-level-graph',
  imports: [LineChartModule, CommonModule, ClrIconModule],
  templateUrl: './water-level-graph.component.html',
  styleUrl: './water-level-graph.component.css',
})
export class WaterLevelGraphComponent {
  // view: [number, number] = [700, 300];
  legend: boolean = true;
  showLabels: boolean = true;
  animations: boolean = true;
  xAxis: boolean = true;
  yAxis: boolean = true;
  showYAxisLabel: boolean = true;
  showXAxisLabel: boolean = true;
  xAxisLabel: string = 'Tiempo';
  timeline: boolean = true;

  colorScheme : any = {
    domain: ['#42A5F5', '#66BB6A', '#FFA726', '#AB47BC', '#EC407A'],
    group: 'Ordinal',
    selectable: true,
    name: 'dark',
  };

  waterLevelHistory: any[] = [
    {
      name: 'Nivel de Agua',
      series: [],
    },
  ];

  waterLevel = 0;

  constructor(private tankService: TankService) {}

  ngOnInit() {
    this.listenerTankStatus();
  }

  getAlertClass(): string {
    if (this.waterLevel < 20) return 'alert-danger';
    if (this.waterLevel < 50) return 'alert-warning';
    return 'alert-success';
  }

  getTankStatus(): string {
    if (this.waterLevel < 20) return 'Nivel Crítico';
    if (this.waterLevel < 50) return 'Nivel Medio';
    return 'Nivel Óptimo';
  }

  listenerTankStatus() {
    this.tankService.getWaterTankCentral().subscribe((message: WaterTank | null) => {
      if (!message) {
        return;
      }

      this.waterLevel = Math.floor(
        (message.currentVolume / message.maximumVolume) * 100
      );

      this.waterLevelHistory[0].series.push({
        name: this.getTimeFormatted(),
        value: this.waterLevel,
      });

      if (this.waterLevelHistory[0].series.length > 20) {
        this.waterLevelHistory[0].series.shift();
      }

      this.waterLevelHistory = [...this.waterLevelHistory];
    });
  }

  private getTimeFormatted() {
    const now = new Date();
    return now.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }
}
