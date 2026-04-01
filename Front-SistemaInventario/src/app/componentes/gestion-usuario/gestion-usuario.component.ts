import { Component, OnInit } from '@angular/core';
import { UsuarioService } from '../../servicios/usuario.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InformacionUsuario } from '../../modelo/informacionObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-gestion-usuario',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './gestion-usuario.component.html'
})
export class GestionUsuarioComponent implements OnInit {
  usuarios: InformacionUsuario[] = [];
  seleccionados: InformacionUsuario[] = [];
  textoBtnEliminar = '';

  constructor(private svc: UsuarioService) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.svc.getUsuarios().subscribe({
      next: (data: MensajeDTO) => this.usuarios = data.respuesta,
      error: (e: any) => console.error(e)
    });
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
      this.svc.inactivarUsuario(u.idUsuario!, motivo).toPromise()
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
    return u.idUsuario;
  }
}
