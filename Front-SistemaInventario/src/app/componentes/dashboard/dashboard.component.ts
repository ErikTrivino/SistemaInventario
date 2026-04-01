import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableroService } from '../../servicios/tablero.service';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  resumen: any = {
    totalProductos: 0,
    bajoStock: 0,
    transferenciasPendientes: 0,
    ventasHoy: 0
  };

  constructor(private tableroService: TableroService) { }

  ngOnInit(): void {
    this.cargarResumen();
  }

  cargarResumen(): void {
    this.tableroService.getResumenDiario().subscribe({
      next: (data: MensajeDTO) => {
        if (data.respuesta) {
          this.resumen = data.respuesta;
        }
      },
      error: (err: any) => {
        console.error('Error al cargar resumen del tablero:', err);
      }
    });
  }
}
