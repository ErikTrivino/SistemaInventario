import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProveedorService } from '../../servicios/proveedor.service';
import Swal from 'sweetalert2';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-crear-proveedor',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './crear-proveedor.component.html'
})
export class CrearProveedorComponent {
  form!: FormGroup;

  constructor(private fb: FormBuilder, private svc: ProveedorService) {
    this.form = fb.group({
      nombre: ['', Validators.required],
      identificacion: ['', Validators.required],
      telefono: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]]
    });
  }

  crear() {
    if (this.form.valid) {
      this.svc.crear(this.form.value).subscribe({
        next: (data: MensajeDTO) => {
          Swal.fire('Éxito', data.respuesta || 'Proveedor creado correctamente', 'success');
          this.form.reset();
        },
        error: (err: any) => {
          console.error(err);
          Swal.fire('Error', 'No se creó el proveedor', 'error');
        }
      });
    } else {
      Swal.fire('Error', 'Completa los campos requeridos', 'error');
    }
  }
}
