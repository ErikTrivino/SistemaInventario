import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { UsuarioService } from '../../servicios/usuario.service';
import Swal from 'sweetalert2';
import { EditarUsuario } from '../../modelo/editarObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-editar-usuario',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './editar-usuario.component.html'
})
export class EditarUsuarioComponent implements OnInit {
  form!: FormGroup;
  idUsuario!: number;

  constructor(
    private fb: FormBuilder,
    private svc: UsuarioService,
    private route: ActivatedRoute
  ) {
    this.form = fb.group({
      idUsuario: [''],
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      identificacion: ['', Validators.required],
      numerophone: ['', Validators.required],
      edad: ['', [Validators.required, Validators.min(0)]],
      correo: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      estado: ['', Validators.required],
      rol: ['', Validators.required],
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const val = params.get('id');
      if (val) {
        this.idUsuario = +val;
        this.svc.consultarPorId(this.idUsuario).subscribe({
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

  editar() {
    if (this.form.valid) {
      this.svc.actualizarUsuario(this.idUsuario, this.form.value).subscribe({
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
