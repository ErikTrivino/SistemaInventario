import { Component, OnInit } from '@angular/core';
import { ProveedorService } from '../../servicios/proveedor.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InformacionProveedor } from '../../modelo/informacionObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-gestion-proveedor',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './gestion-proveedor.component.html',
})
export class GestionProveedorComponent implements OnInit {
  proveedores: InformacionProveedor[] = [];
  seleccionados: InformacionProveedor[] = [];
  textoBtnAccion = '';

  constructor(private svc: ProveedorService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.svc.listar().subscribe({
      next: (data: MensajeDTO) => (this.proveedores = data.respuesta),
      error: (e: any) => console.error(e),
    });
  }

  seleccionar(item: InformacionProveedor, sel: boolean) {
    if (sel) {
      if (!this.seleccionados.includes(item)) {
        this.seleccionados.push(item);
      }
    } else {
      const index = this.seleccionados.indexOf(item);
      if (index !== -1) {
        this.seleccionados.splice(index, 1);
      }
    }
    this.textoBtnAccion = `${this.seleccionados.length} proveedor${this.seleccionados.length === 1 ? '' : 'es'}`;
  }

  confirmarToggleEstado() {
    Swal.fire({
      title: '¿Cambiar estado?',
      text: `Se cambiará el estado de ${this.seleccionados.length} proveedor(es).`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, cambiar',
      cancelButtonText: 'Cancelar'
    }).then((r) => {
      if (r.isConfirmed) this.toggleEstadoSelec();
    });
  }

  toggleEstadoSelec() {
    const promises = this.seleccionados.map(p => this.svc.toggleEstado(p.idProveedor).toPromise());

    Promise.all(promises)
      .then(() => {
        Swal.fire('Procesado', 'El estado de los proveedores ha sido actualizado', 'success');
        this.cargar();
        this.seleccionados = [];
        this.textoBtnAccion = '';
      })
      .catch((err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudo actualizar el estado de algunos proveedores', 'error');
      });
  }

  trackById(_i: number, p: InformacionProveedor) {
    return p.idProveedor;
  }
}
