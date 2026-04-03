import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../servicios/reporte.service';
import { SucursalService } from '../../servicios/sucursal.service';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { 
  ReporteVentasDTO, 
  ReporteInventarioDTO, 
  ReporteTransferenciasDTO, 
  ReporteComparativoDTO, 
  ReporteRotacionDTO
} from '../../modelo/informacionObjeto';
import Swal from 'sweetalert2';
import { PaginadorComponent } from '../comun/paginador/paginador.component';

@Component({
  selector: 'app-gestion-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginadorComponent],
  templateUrl: './gestion-reportes.component.html',
  styleUrls: ['./gestion-reportes.component.css']
})
export class GestionReportesComponent implements OnInit {
  // Navigation State
  tipoReporte: 'VENTAS' | 'INVENTARIO' | 'TRANSFERENCIAS' | 'COMPARATIVO' | 'ROTACION' | null = null;
  cargando = false;

  // Filters
  fechaInicio: string = '';
  fechaFin: string = '';
  idSucursal: number | null = null;
  anio: number = new Date().getFullYear();
  mes: number = new Date().getMonth() + 1;

  // Data
  sucursales: any[] = [];
  reporteVentas: ReporteVentasDTO | null = null;
  reporteInventario: ReporteInventarioDTO | null = null;
  reporteTransferencias: ReporteTransferenciasDTO | null = null;
  reporteComparativo: ReporteComparativoDTO | null = null;
  reporteRotacion: ReporteRotacionDTO | null = null;

  // Pagination
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  // Utils
  meses = [
    { n: 1, nombre: 'Enero' }, { n: 2, nombre: 'Febrero' }, { n: 3, nombre: 'Marzo' },
    { n: 4, nombre: 'Abril' }, { n: 5, nombre: 'Mayo' }, { n: 6, nombre: 'Junio' },
    { n: 7, nombre: 'Julio' }, { n: 8, nombre: 'Agosto' }, { n: 9, nombre: 'Septiembre' },
    { n: 10, nombre: 'Octubre' }, { n: 11, nombre: 'Noviembre' }, { n: 12, nombre: 'Diciembre' }
  ];

  constructor(
    private reporteSvc: ReporteService,
    private sucursalSvc: SucursalService
  ) {}

  ngOnInit() {
    this.cargarSucursales();
    // Default dates
    const today = new Date();
    const lastMonth = new Date();
    lastMonth.setMonth(today.getMonth() - 1);
    this.fechaFin = today.toISOString().split('T')[0];
    this.fechaInicio = lastMonth.toISOString().split('T')[0];
  }

  cargarSucursales() {
    this.sucursalSvc.listar().subscribe({
      next: (res) => this.sucursales = res.respuesta,
      error: (err) => console.error('Error sucursales', err)
    });
  }

  seleccionarReporte(tipo: 'VENTAS' | 'INVENTARIO' | 'TRANSFERENCIAS' | 'COMPARATIVO' | 'ROTACION') {
    this.tipoReporte = tipo;
    this.limpiarDatos();
  }

  limpiarDatos() {
    this.reporteVentas = null;
    this.reporteInventario = null;
    this.reporteTransferencias = null;
    this.reporteComparativo = null;
    this.reporteRotacion = null;
    this.paginaActual = 0;
    this.totalElementos = 0;
    this.totalPaginas = 0;
  }

  generar() {
    if (!this.tipoReporte) return;
    this.cargando = true;
    
    switch (this.tipoReporte) {
      case 'VENTAS':
        this.reporteSvc.generarReporteVentas(this.fechaInicio, this.fechaFin, this.paginaActual, this.tamanoPagina).subscribe({
          next: (res) => {
            this.reporteVentas = res.respuesta;
            this.totalElementos = this.reporteVentas?.porSucursal.totalElements || 0;
            this.totalPaginas = this.reporteVentas?.porSucursal.totalPages || 0;
            this.cargando = false;
          },
          error: (err) => this.manejarError(err)
        });
        break;
      case 'INVENTARIO':
        this.reporteSvc.generarReporteInventario(this.idSucursal || undefined, this.paginaActual, this.tamanoPagina).subscribe({
          next: (res) => {
            this.reporteInventario = res.respuesta;
            this.totalElementos = this.reporteInventario?.detalle.totalElements || 0;
            this.totalPaginas = this.reporteInventario?.detalle.totalPages || 0;
            this.cargando = false;
          },
          error: (err) => this.manejarError(err)
        });
        break;
      case 'TRANSFERENCIAS':
        this.reporteSvc.generarReporteTransferencias(this.fechaInicio, this.fechaFin, this.paginaActual, this.tamanoPagina).subscribe({
          next: (res) => {
            this.reporteTransferencias = res.respuesta;
            this.totalElementos = this.reporteTransferencias?.detalle.totalElements || 0;
            this.totalPaginas = this.reporteTransferencias?.detalle.totalPages || 0;
            this.cargando = false;
          },
          error: (err) => this.manejarError(err)
        });
        break;
      case 'COMPARATIVO':
        this.reporteSvc.generarComparativoAnual(this.anio).subscribe({
          next: (res) => {
            this.reporteComparativo = res.respuesta;
            this.totalElementos = 0;
            this.totalPaginas = 0;
            this.cargando = false;
          },
          error: (err) => this.manejarError(err)
        });
        break;
      case 'ROTACION':
        this.reporteSvc.generarAnalisisRotacion(this.mes, this.anio, this.paginaActual, this.tamanoPagina).subscribe({
          next: (res) => {
            this.reporteRotacion = res.respuesta;
            this.totalElementos = this.reporteRotacion?.productos.totalElements || 0;
            this.totalPaginas = this.reporteRotacion?.productos.totalPages || 0;
            this.cargando = false;
          },
          error: (err) => this.manejarError(err)
        });
        break;
    }
  }

  manejarError(err: any) {
    this.cargando = false;
    Swal.fire('Error', 'No se pudo generar el reporte: ' + (err.error?.mensaje || 'Error desconocido'), 'error');
  }

  onCambioPagina(p: number) {
    this.paginaActual = p;
    this.generar();
  }

  getNombreSucursal(id: number) {
    return this.sucursales.find(s => s.id === id)?.nombre || `Sucursal ${id}`;
  }
}
