import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  ClrSidePanelModule,
  ClrFormsModule,
  ClrInputModule,
} from '@clr/angular';
import { HouseService } from '../../services/house.service';
import { pipe } from 'rxjs';
import { HouseDto } from '../../models/house.mode';
import { WaterTankRequest } from '../../models/water-tank-request';
import { Notyf } from 'notyf';
import { HttpResponse } from '@angular/common/http';

export interface HouseTankFormValue {
  tankHeight: number;
  tankDiameter: number;
  pipeDiameter: string;
  pipeLength: number;
}

@Component({
  selector: 'app-house-form',
  standalone: true,
  imports: [
    ClrSidePanelModule,
    ClrFormsModule,
    ClrInputModule,
    CommonModule,
    ReactiveFormsModule,
  ],
  templateUrl: './house-form.component.html',
  styleUrl: './house-form.component.css',
})
export class HouseFormComponent implements OnInit {
  @Input() opened: boolean = false;
  @Output() closed = new EventEmitter<void>();

  dropdownData: { key: string; value: string }[] = [];

  form: FormGroup;
  pipeOptions: { key: string; value: string }[] = [];


  constructor(
    private houseService: HouseService,
    private formBuilder: FormBuilder
  ) {
    this.form = this.formBuilder.group({
      tankHeight: ['', [Validators.required, Validators.min(0.1)]],
      tankDiameter: ['', [Validators.required, Validators.min(0.1)]],
      pipeLength: ['', [Validators.required, Validators.min(0.1)]],
      pipeDiameter: ['', [Validators.required]],
    });
  }

  onSave() {
    if (this.form.valid) {
      const house: WaterTankRequest = {
        tankHeight: this.form.get('tankHeight')?.value,
        tankDiameter: this.form.get('tankDiameter')?.value,
        pipeLength: this.form.get('pipeLength')?.value,
        pipeDiameter: this.form.get('pipeDiameter')?.value,
      };
      this.onCancel();
      this.houseService.postHouseWaterTank(house).subscribe({
        next: (response : HttpResponse<void>) => {
          if(response.ok){

          }
        },
        error: (error) => {
          
        }
      });
    }
  }

  private loadDropdownData() {
    this.houseService.getPipes().subscribe(
      (data) => {
        this.dropdownData = data;
      },
      (error) => {
        console.log('Error: ', error);
      }
    );
  }

  onCancel() {
    this.closed.emit();
  }

  ngOnInit(): void {
    this.loadDropdownData();
  }
}
