import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectionStrategy,
} from '@angular/core';
import { ClrIconModule, ClrDatagridModule } from '@clr/angular';
import { TankService } from '../../services/tank.service';
import { HouseDto } from '../../models/house.mode';
import { CommonModule } from '@angular/common';
import { BehaviorSubject, Subscription } from 'rxjs';

@Component({
  selector: 'app-consumers',
  imports: [ClrIconModule, ClrDatagridModule, CommonModule],
  templateUrl: './consumers.component.html',
  styleUrl: './consumers.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConsumersComponent implements OnInit, OnDestroy {
  private consumersSubject = new BehaviorSubject<HouseDto[]>([]);
  consumers$ = this.consumersSubject.asObservable();

  private subscription = new Subscription();

  constructor(private tankService: TankService) {}

  ngOnInit(): void {
    this.initListeners();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private initListeners() {
    const sub = this.tankService
      .getWaterTankHouse()
      .subscribe((message: HouseDto | null) => {
        if (!message) return;

        // Keep updates immutable: get current value, produce a new array
        const current = this.consumersSubject.getValue();

        const idx = current.findIndex((f) => f.id === message.id);

        if (idx === -1) {
          // add new house
          this.consumersSubject.next([...current, message]);
          return;
        }

        // merge existing house with incoming partial update
        const existing = current[idx];
        const merged: HouseDto = {
          ...existing,
          ...message,
          // only merge nested waterTank if incoming message contains it
          waterTank: message.waterTank
            ? { ...existing?.waterTank, ...message.waterTank }
            : existing.waterTank,
        };

        const next = [...current];
        next[idx] = merged;
        this.consumersSubject.next(next);
      });

    this.subscription.add(sub);
  }

  // trackBy for datagrid performance
  trackById(index: number, item: HouseDto) {
    return item?.id ?? index;
  }
}
