import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InventarioService } from '../../servicios/inventario.service';
import { ProveedorService } from '../../servicios/proveedor.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-crear-producto',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './crear-producto.component.html'
})
export class CrearProductoComponent implements OnInit {
  form!: FormGroup;
  proveedores: any[] = [];

  constructor(
    private fb: FormBuilder,
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: ['', Validators.required],
      sku: ['', [Validators.required, Validators.maxLength(50)]],
      unidadMedidaBase: ['UND', Validators.required],
      precioCostoPromedio: [0, [Validators.required, Validators.min(0)]],
      cantidadInicial: [0, [Validators.required, Validators.min(0)]],
      idSucursal: [1, Validators.required] // Por defecto sucursal 1 (Central)
    });
  }

  ngOnInit(): void {
    this.cargarProveedores();
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
        Swal.fire('Error', 'No se pudo cargar la lista de proveedores', 'error');
      }
    });
  }

  crear(): void {
    if (this.form.valid) {
      // Map form to backend expected DTO structure if needed
      // Current implementation assumes service handles the structure or DTO matches
      this.inventarioService.createProduct(this.form.value).subscribe({
        next: (data: MensajeDTO) => {
          Swal.fire('Éxito', data.respuesta || 'Producto creado correctamente', 'success');
          this.form.reset();
        },
        error: (err: any) => {
          console.error(err);
          Swal.fire('Error', 'No se pudo crear el producto', 'error');
        }
      });
    } else {
      Swal.fire('Error', 'Revisa los campos obligatorios', 'error');
    }
  }
}
