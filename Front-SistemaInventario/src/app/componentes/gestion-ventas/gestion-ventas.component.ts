import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-gestion-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-ventas.component.html',
  styleUrl: './gestion-ventas.component.css'
})
export class GestionVentasComponent implements OnInit {

  ngOnInit(): void {}
}
