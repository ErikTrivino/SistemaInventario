import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InventarioService } from '../../servicios/inventario.service';
import { SucursalService } from '../../servicios/sucursal.service';
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
  sucursales: any[] = [];

  constructor(
    private fb: FormBuilder,
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService,
    private sucursalService: SucursalService
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: [''],
      sku: ['', [Validators.maxLength(50)]],
      unidadMedidaBase: ['UND', Validators.required],
      precioCostoPromedio: [0, [Validators.required, Validators.min(0)]],
      activo: [true],
      cantidadInicial: [0, [Validators.required, Validators.min(0)]],
      idSucursal: [1, Validators.required], // Por defecto sucursal 1 (Central)
      idProveedor: [null, Validators.required],
      cantidadMinima: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.cargarProveedores();
    this.cargarSucursales();
  }

  cargarSucursales(): void {
    this.sucursalService.listar().subscribe({
      next: (data: MensajeDTO) => {
        this.sucursales = data.respuesta;
      },
      error: (err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudo cargar la lista de sucursales', 'error');
      }
    });
  }

  cargarProveedores() {
    this.proveedorService.listar().subscribe({
      next: (data: MensajeDTO) => {
        const content = data.respuesta.content || data.respuesta;
        this.proveedores = content.map((prov: any) => ({
          id: prov.id,
          label: prov.razonSocial
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
      const dto = {
        ...this.form.value,
        precioCostoPromedio: Number(this.form.value.precioCostoPromedio),
        cantidadInicial: Number(this.form.value.cantidadInicial),
        cantidadMinima: Number(this.form.value.cantidadMinima),
        activo: this.form.value.activo,
        idProveedor: Number(this.form.value.idProveedor),
        idSucursal: Number(this.form.value.idSucursal)
      };

      console.log(dto);
      this.inventarioService.createProduct(dto).subscribe({
        next: (data: MensajeDTO) => {
          Swal.fire('Éxito', 'Producto creado correctamente', 'success');
          this.form.reset({
            unidadMedidaBase: 'UND',
            idSucursal: 1,
            activo: true,
            precioCostoPromedio: 0,
            cantidadInicial: 0,
            cantidadMinima: 0
          });
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
