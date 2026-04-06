import { Component, OnInit } from '@angular/core';
import { UsuarioService } from '../../servicios/usuario.service';
import { UsuarioConsultaService } from '../../servicios/usuario-consulta.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InformacionUsuario } from '../../modelo/informacionObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import { FormsModule } from '@angular/forms';
import { SucursalService } from '../../servicios/sucursal.service';

@Component({
  selector: 'app-gestion-usuario',
  standalone: true,
  imports: [CommonModule, RouterModule, PaginadorComponent, FormsModule],
  templateUrl: './gestion-usuario.component.html'
})
export class GestionUsuarioComponent implements OnInit {
  usuarios: InformacionUsuario[] = [];
  seleccionados: InformacionUsuario[] = [];
  textoBtnEliminar = '';

  // Estado de paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  // Filtros
  filtroNombre = '';
  filtroRol = '';
  filtroSucursal: number | null = null;

  sucursales: any[] = [];

  constructor(
    private svc: UsuarioService,
    private usuarioConsultaSvc: UsuarioConsultaService,
    private sucursalSvc: SucursalService
  ) {}

  ngOnInit() {
    this.cargar();
    this.cargarSucursales();
  }

  cargarSucursales() {
    this.sucursalSvc.listar().subscribe({
      next: (data: MensajeDTO) => {
        this.sucursales = data.respuesta;
      },
      error: (e: any) => console.error('Error cargando sucursales', e)
    });
  }

  cargar() {
    if (this.filtroNombre || this.filtroRol || this.filtroSucursal) {
      this.aplicarFiltros();
      return;
    }

    this.usuarioConsultaSvc.getUsuarios(this.paginaActual + 1, this.tamanoPagina).subscribe({
      next: (data: MensajeDTO) => {
        if (data.respuesta.content) {
          this.usuarios = data.respuesta.content;
          this.totalElementos = data.respuesta.totalElements;
          this.totalPaginas = data.respuesta.totalPages;
          this.paginaActual = data.respuesta.number;
        } else {
          this.usuarios = data.respuesta;
          this.totalElementos = this.usuarios.length;
          this.totalPaginas = 1;
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  aplicarFiltros() {
    // Si hay búsqueda por nombre
    if (this.filtroNombre) {
      this.usuarioConsultaSvc.buscarPorNombre(this.filtroNombre, true).subscribe({
        next: (data: MensajeDTO) => {
          this.usuarios = data.respuesta;
          this.actualizarMetadatosLocales();
        }
      });
    } else if (this.filtroRol) {
      this.usuarioConsultaSvc.filtrarPorRol(this.filtroRol).subscribe({
        next: (data: MensajeDTO) => {
          this.usuarios = data.respuesta;
          this.actualizarMetadatosLocales();
        }
      });
    } else if (this.filtroSucursal) {
      this.usuarioConsultaSvc.filtrarPorSucursal(this.filtroSucursal).subscribe({
        next: (data: MensajeDTO) => {
          this.usuarios = data.respuesta;
          this.actualizarMetadatosLocales();
        }
      });
    }
  }

  private actualizarMetadatosLocales() {
    this.totalElementos = this.usuarios.length;
    this.totalPaginas = 1;
    this.paginaActual = 0;
  }

  onCambioPagina(p: number) {
    this.paginaActual = p;
    this.cargar();
  }

  onCambioTamano(t: number) {
    this.tamanoPagina = t;
    this.paginaActual = 0;
    this.cargar();
  }

  limpiarFiltros() {
    this.filtroNombre = '';
    this.filtroRol = '';
    this.filtroSucursal = null;
    this.paginaActual = 0;
    this.cargar();
  }

  seleccionar(u: InformacionUsuario, sel: boolean) {
    if (sel) {
      if (!this.seleccionados.includes(u)) {
        this.seleccionados.push(u);
      }
    } else {
      const index = this.seleccionados.indexOf(u);
      if (index !== -1) {
        this.seleccionados.splice(index, 1);
      }
    }
    this.textoBtnEliminar = `${this.seleccionados.length} usuario${this.seleccionados.length === 1 ? '' : 's'}`;
  }

  confirmarInactivar() {
    Swal.fire({
      title: '¿Inactivar usuarios?',
      text: `Se inactivarán ${this.seleccionados.length} usuario(s).`,
      icon: 'warning',
      input: 'text',
      inputPlaceholder: 'Motivo de inactivación (opcional)',
      showCancelButton: true,
      confirmButtonText: 'Sí, inactivar',
      cancelButtonText: 'Cancelar'
    }).then(r => {
      if (r.isConfirmed) {
        const motivo = r.value || 'Manual inactivation via management interface';
        this.inactivar(motivo);
      }
    });
  }

  inactivar(motivo: string) {
    const promises = this.seleccionados.map(u =>
      this.svc.inactivarUsuario(u.id!, motivo).toPromise()
    );

    Promise.all(promises)
      .then(() => {
        Swal.fire('Procesado', 'Los usuarios seleccionados han sido inactivados', 'success');
        this.cargar();
        this.seleccionados = [];
        this.textoBtnEliminar = '';
      })
      .catch((err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudieron inactivar algunos usuarios', 'error');
      });
  }

  trackById(_i: number, u: InformacionUsuario) {
    return u.id;
  }
}
