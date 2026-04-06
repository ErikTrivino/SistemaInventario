import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { UsuarioService } from '../../servicios/usuario.service';
import { UsuarioConsultaService } from '../../servicios/usuario-consulta.service';
import Swal from 'sweetalert2';
import { EditarUsuario } from '../../modelo/editarObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { CommonModule } from '@angular/common';
import { SucursalService } from '../../servicios/sucursal.service';

@Component({
  selector: 'app-editar-usuario',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './editar-usuario.component.html'
})
export class EditarUsuarioComponent implements OnInit {
  form!: FormGroup;
  id!: number;
  roles: string[] = ['ADMIN', 'GERENTE', 'OPERADOR'];
  sucursales: any[] = [];

  constructor(
    private fb: FormBuilder,
    private svc: UsuarioService,
    private usuarioConsultaSvc: UsuarioConsultaService,
    private sucursalSvc: SucursalService,
    private route: ActivatedRoute
  ) {
    this.form = fb.group({
      id: [null],
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      password: [''],
      activo: [true],
      rol: ['', Validators.required],
      sucursalAsignadaId: [null, Validators.required],
      motivoInactivacion: ['']
    });
  }

  ngOnInit() {
    this.cargarSucursales();
    this.route.paramMap.subscribe(params => {
      const val = params.get('id');
      if (val) {
        this.id = +val;
        this.usuarioConsultaSvc.consultarPorId(this.id).subscribe({
          next: (data: MensajeDTO) => {
            this.form.patchValue(data.respuesta);
            this.form.patchValue({ password: '' });
          },
          error: (err: any) => {
            console.error(err);
            Swal.fire('Error', 'No se cargó el usuario', 'error');
          }
        });
      }
    });
  }

  cargarSucursales() {
    this.sucursalSvc.listar().subscribe({
      next: (data: MensajeDTO) => {
        this.sucursales = data.respuesta;
      },
      error: (err: any) => {
        console.error('Error cargando sucursales', err);
      }
    });
  }

  editar() {
    if (this.form.valid) {
      this.svc.actualizarUsuario(this.id, this.form.value).subscribe({
        next: (data: MensajeDTO) => Swal.fire('Actualizado', data.respuesta || 'Usuario actualizado', 'success'),
        error: (err: any) => {
          console.error(err);
          Swal.fire('Error', 'No se pudo actualizar', 'error');
        }
      });
    } else {
      Swal.fire('Error', 'Revisa los campos obligatorios', 'error');
    }
  }
}
