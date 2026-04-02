import { Component, OnInit } from '@angular/core';
import { ProveedorService } from '../../servicios/proveedor.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InformacionProveedor } from '../../modelo/informacionObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-gestion-provider',
  standalone: true,
  imports: [CommonModule, RouterModule, PaginadorComponent, FormsModule],
  templateUrl: './gestion-proveedor.component.html',
})
export class GestionProveedorComponent implements OnInit {

  proveedores: InformacionProveedor[] = [];
  seleccionados: InformacionProveedor[] = [];

  textoBtnAccion = '';

  // Paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  // Filtro
  mostrarTodos = false;

  loading = false;

  constructor(private svc: ProveedorService) { }

  ngOnInit(): void {
    this.cargar();
  }

  // ========================
  // CARGA DE DATOS
  // ========================
  cargar(): void {
    this.loading = true;

    const request = this.mostrarTodos
      ? this.svc.listarTodos(this.paginaActual, this.tamanoPagina)
      : this.svc.listar(this.paginaActual, this.tamanoPagina);

    request.subscribe({
      next: (data: MensajeDTO) => {
        const respuesta = data.respuesta;

        // 🔥 Manejo unificado de respuesta paginada o simple
        if (respuesta?.content) {
          this.proveedores = respuesta.content;
          this.totalElementos = respuesta.totalElements;
          this.totalPaginas = respuesta.totalPages;
          this.paginaActual = respuesta.number;
        } else {
          this.proveedores = respuesta || [];
          this.totalElementos = this.proveedores.length;
          this.totalPaginas = 1;
          this.paginaActual = 0;
        }

        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        Swal.fire('Error', 'No se pudieron cargar los proveedores', 'error');
      }
    });
  }

  // ========================
  // PAGINACIÓN
  // ========================
  onCambioPagina(p: number) {
    this.paginaActual = p;
    this.cargar();
  }

  onCambioTamano(t: number) {
    this.tamanoPagina = t;
    this.paginaActual = 0;
    this.cargar();
  }

  toggleMostrarTodos() {
    this.mostrarTodos = !this.mostrarTodos;
    this.paginaActual = 0;
    this.cargar();
  }

  // ========================
  // SELECCIÓN
  // ========================
  seleccionar(item: InformacionProveedor, checked: boolean) {

    if (checked) {
      if (!this.seleccionados.some(p => p.id === item.id)) {
        this.seleccionados.push(item);
      }
    } else {
      this.seleccionados = this.seleccionados.filter(p => p.id !== item.id);
    }

    this.actualizarTextoBoton();
  }

  actualizarTextoBoton() {
    const n = this.seleccionados.length;
    this.textoBtnAccion = n > 0
      ? `${n} proveedor${n === 1 ? '' : 'es'}`
      : '';
  }

  // ========================
  // ACCIONES
  // ========================
  toggleEstadoSelec() {

    if (this.seleccionados.length === 0) return;

    const requests = this.seleccionados.map(p =>
      this.svc.toggleEstado(p.id)
    );

    forkJoin(requests).subscribe({
      next: () => {
        Swal.fire('Procesado', 'Estado actualizado correctamente', 'success');
        this.seleccionados = [];
        this.textoBtnAccion = '';
        this.cargar();
      },
      error: (err) => {
        console.error(err);
        Swal.fire('Error', 'Falló la actualización de algunos proveedores', 'error');
      }
    });
  }

  // ========================
  // OPTIMIZACIÓN
  // ========================
  trackById(_i: number, p: InformacionProveedor) {
    return p.id;
  }
}