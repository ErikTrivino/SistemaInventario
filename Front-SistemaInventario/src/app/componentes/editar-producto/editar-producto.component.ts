import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { InventarioService } from '../../servicios/inventario.service';
import { ProveedorService } from '../../servicios/proveedor.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-editar-producto',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './editar-producto.component.html'
})
export class EditarProductoComponent implements OnInit {
  form!: FormGroup;
  id!: number;
  proveedores: any[] = [];

  constructor(
    private fb: FormBuilder,
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: ['', Validators.required],
      precio: [0, [Validators.required, Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      idProveedor: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.cargarProveedores();
    this.route.paramMap.subscribe(params => {
      const val = params.get('id');
      if (val) {
        this.id = +val;
        this.cargarProducto();
      }
    });
  }

  cargarProveedores() {
    this.proveedorService.listar().subscribe({
      next: (data: MensajeDTO) => {
        this.proveedores = data.respuesta.map((prov: any) => ({
          id: prov.idProveedor,
          label: `${prov.nombre} - ${prov.identificacion}`
        }));
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }

  cargarProducto() {
    this.inventarioService.consultarPorId(this.id).subscribe({
      next: (data: MensajeDTO) => {
        this.form.patchValue(data.respuesta);
      },
      error: (err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se cargó el producto', 'error');
      }
    });
  }

  editar() {
    if (this.form.valid) {
      this.inventarioService.updateProduct(this.id, this.form.value).subscribe({
        next: (data: MensajeDTO) => Swal.fire('Actualizado', data.respuesta || 'Producto actualizado', 'success'),
        error: (err: any) => {
          console.error(err);
          Swal.fire('Error', 'No se pudo actualizar el producto', 'error');
        }
      });
    } else {
      Swal.fire('Error', 'Revisa los campos obligatorios', 'error');
    }
  }
}
