import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoService } from '../../servicios/movimiento.service';
import { InventarioService } from '../../servicios/inventario.service';
import { SucursalService } from '../../servicios/sucursal.service';
import { InformacionMovimientoDTO, TipoMovimiento, InformacionProducto } from '../../modelo/informacionObjeto';

@Component({
  selector: 'app-gestion-movimientos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-movimientos.component.html',
  styleUrl: './gestion-movimientos.component.css'
})
export class GestionMovimientosComponent implements OnInit {

  movimientos: InformacionMovimientoDTO[] = [];
  productos: InformacionProducto[] = [];
  sucursales: any[] = [];

  // Filtros
  productoId?: number;
  sucursalId?: number;
  fechaInicio: string = '';
  fechaFin: string = '';

  // Paginación
  paginaActual: number = 0;
  totalElementos: number = 0;
  totalPaginas: number = 0;
  tamanoPagina: number = 10;

  constructor(
    private movimientoService: MovimientoService,
    private inventarioService: InventarioService,
    private sucursalService: SucursalService
  ) {}

  ngOnInit(): void {
    this.setDefaultDates();
    this.cargarProductos();
    this.cargarSucursales();
    this.buscar();
  }

  setDefaultDates(): void {
    const today = new Date();
    // Primer día del mes
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    // Formato YYYY-MM-DD para el input date
    this.fechaInicio = this.formatDate(firstDay);
    this.fechaFin = this.formatDate(today);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  cargarProductos(): void {
    this.inventarioService.getProducts(0, 100).subscribe(res => {
      if (res.error === false) {
        this.productos = res.respuesta.content;
      }
    });
  }

  cargarSucursales(): void {
    this.sucursalService.listar().subscribe(res => {
      if (res.error === false) {
        this.sucursales = res.respuesta;
      }
    });
  }

  buscar(pagina: number = 0): void {
    this.paginaActual = pagina;
    
    // Convertir fechas a ISO para el backend (agregando hora para cubrir todo el día)
    const inicio = this.fechaInicio ? `${this.fechaInicio}T00:00:00` : undefined;
    const fin = this.fechaFin ? `${this.fechaFin}T23:59:59` : undefined;

    this.movimientoService.buscarMovimientos(
      this.productoId,
      this.sucursalId,
      inicio,
      fin,
      this.paginaActual,
      this.tamanoPagina
    ).subscribe(res => {
      if (res.error === false) {
        this.movimientos = res.respuesta.content;
        this.totalElementos = res.respuesta.totalElements;
        this.totalPaginas = res.respuesta.totalPages;
      }
    });
  }

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina >= 0 && nuevaPagina < this.totalPaginas) {
      this.buscar(nuevaPagina);
    }
  }

  getTipoBadgeClass(tipo: TipoMovimiento): string {
    switch (tipo) {
      case TipoMovimiento.ENTRADA_COMPRA:
      case TipoMovimiento.TRANSFERENCIA_RECIBO:
        return 'bg-green-100 text-green-800';
      case TipoMovimiento.SALIDA_VENTA:
      case TipoMovimiento.TRANSFERENCIA_ENVIO:
      case TipoMovimiento.MERMA:
        return 'bg-red-100 text-red-800';
      case TipoMovimiento.AJUSTE:
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatTipo(tipo: string): string {
    // Reemplaza guiones bajos por espacios y capitaliza
    return tipo.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  }
}
