import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditoriaService } from '../../servicios/auditoria.service';
import { UsuarioService } from '../../servicios/usuario.service';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { InformacionUsuario } from '../../modelo/informacionObjeto';
import { PaginadorComponent } from '../comun/paginador/paginador.component';

@Component({
  selector: 'app-gestion-auditorias',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginadorComponent],
  templateUrl: './gestion-auditorias.component.html',
  styles: ``
})
export class GestionAuditoriasComponent implements OnInit {
  logs: any[] = [];
  usuarios: InformacionUsuario[] = [];

  // Paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  porPagina = 10;

  // Filtros
  filtroUsuarioId = '';
  filtroUsuarioSeleccionado: string = '';

  constructor(
    private auditoriaSvc: AuditoriaService,
    private usuarioSvc: UsuarioService
  ) { }

  ngOnInit(): void {
    this.cargarLogs();
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    // Cargamos todos los usuarios para el dropdown (podría paginarse si son muchos,
    // pero para filtros usualmente se cargan los principales o se usa autocomplete)
    this.usuarioSvc.getUsuarios(1, 100).subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error) {
          this.usuarios = data.respuesta.content || data.respuesta;
        }
      }
    });
  }

  cargarLogs(pagina: number = 0): void {
    this.paginaActual = pagina;
    if (this.filtroUsuarioId || this.filtroUsuarioSeleccionado) {
      const idABuscar = this.filtroUsuarioId || this.filtroUsuarioSeleccionado;
      this.auditoriaSvc.getAuditLogsByUser(idABuscar, this.paginaActual + 1, this.porPagina).subscribe({
        next: (data: MensajeDTO) => this.procesarRespuesta(data),
        error: (e) => console.error(e)
      });
    } else {
      this.auditoriaSvc.getAuditLogs(this.paginaActual + 1, this.porPagina).subscribe({
        next: (data: MensajeDTO) => this.procesarRespuesta(data),
        error: (e) => console.error(e)
      });
    }
  }

  private procesarRespuesta(data: MensajeDTO): void {
    if (!data.error) {
      if (data.respuesta.content) {
        this.logs = data.respuesta.content;
        this.totalPaginas = data.respuesta.totalPages;
        this.totalElementos = data.respuesta.totalElements;
      } else {
        this.logs = data.respuesta;
        this.totalPaginas = 1;
        this.totalElementos = this.logs.length;
        this.paginaActual = 0;
      }
    }
  }

  cambiarPagina(nuevaPagina: number): void {
    this.cargarLogs(nuevaPagina);
  }

  onCambioTamano(t: number): void {
    this.porPagina = t;
    this.cargarLogs(0);
  }

  aplicarFiltros(): void {
    this.cargarLogs(0);
  }

  limpiarFiltros(): void {
    this.filtroUsuarioId = '';
    this.filtroUsuarioSeleccionado = '';
    this.cargarLogs(0);
  }

  getIconForActivity(accion: string): string {
    switch (accion?.toLowerCase()) {
      case 'receive': return 'fa-download text-indigo-500';
      case 'create': return 'fa-plus-circle text-green-500';
      case 'update': return 'fa-edit text-blue-500';
      case 'delete': return 'fa-trash-alt text-red-500';
      case 'venta': return 'fa-shopping-cart text-green-500';
      case 'transferencia': return 'fa-exchange-alt text-blue-500';
      case 'ajuste': return 'fa-tools text-orange-500';
      case 'login': return 'fa-user-check text-purple-500';
      default: return 'fa-info-circle text-gray-500';
    }
  }
}

