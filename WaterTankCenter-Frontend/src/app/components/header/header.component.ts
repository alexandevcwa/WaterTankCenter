import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ClarityModule } from '@clr/angular';

@Component({
  selector: 'app-header',
  imports: [ClarityModule, CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  title = 'Sistema de Monitoreo de Tanque de Agua';
}
