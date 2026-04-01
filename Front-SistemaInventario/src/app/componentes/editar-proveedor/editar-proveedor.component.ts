import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProveedorService } from '../../servicios/proveedor.service';
import Swal from 'sweetalert2';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-editar-proveedor',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './editar-proveedor.component.html'
})
export class EditarProveedorComponent implements OnInit {
  form!: FormGroup;
  id!: number;

  constructor(
    private fb: FormBuilder,
    private svc: ProveedorService,
    private route: ActivatedRoute
  ) {
    this.form = fb.group({
      nombre: ['', Validators.required],
      identificacion: ['', Validators.required],
      telefono: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const val = params.get('id');
      if (val) {
        this.id = +val;
        this.svc.consultarPorId(this.id).subscribe({
          next: (data: MensajeDTO) => {
            this.form.patchValue(data.respuesta);
          },
          error: (err: any) => {
            console.error(err);
            Swal.fire('Error', 'No se cargó el proveedor', 'error');
          }
        });
      }
    });
  }

  editar() {
    if (this.form.valid) {
      this.svc.actualizar(this.id, this.form.value).subscribe({
        next: (data: MensajeDTO) => Swal.fire('Actualizado', data.respuesta || 'Proveedor actualizado', 'success'),
        error: (err: any) => {
          console.error(err);
          Swal.fire('Error', 'No se actualizó el proveedor', 'error');
        }
      });
    } else {
      Swal.fire('Error', 'Completa los campos obligatorios', 'error');
    }
  }
}
