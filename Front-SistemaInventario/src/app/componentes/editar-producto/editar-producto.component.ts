import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { InventarioService } from '../../servicios/inventario.service';
import { ProveedorService } from '../../servicios/proveedor.service';
import { SucursalService } from '../../servicios/sucursal.service';
import { TokenService } from '../../servicios/token.service';
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
  idSucursal!: number;
  proveedores: any[] = [];
  sucursales: any[] = [];

  constructor(
    private fb: FormBuilder,
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService,
    private sucursalService: SucursalService,
    private tokenService: TokenService,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: ['', Validators.required],
      sku: ['', [Validators.required, Validators.maxLength(50)]],
      unidadMedidaBase: ['', Validators.required],
      precioCostoPromedio: [0, [Validators.required, Validators.min(0)]],
      stock: [{ value: 0, disabled: false }, [Validators.required, Validators.min(0)]],
      activo: [true],
      idSucursal: ['', Validators.required],
      idProveedor: [null, Validators.required],
      razonCambio: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  ngOnInit() {
    this.cargarProveedores();
    this.cargarSucursales();
    this.route.paramMap.subscribe(params => {
      const valId = params.get('id');
      const valSucursal = params.get('idSucursal');
      if (valId && valSucursal) {
        this.id = +valId;
        this.idSucursal = +valSucursal;
        this.cargarProducto();
      }
    });
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
          label: prov.razonSocial || prov.nombre
        }));
      },
      error: (err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudo cargar la lista de proveedores', 'error');
      }
    });
  }

  cargarProducto() {
    this.inventarioService.getProductByIdSucursal(this.idSucursal, this.id).subscribe({
      next: (data: MensajeDTO) => {
        const prod = data.respuesta;
        this.form.patchValue({
          nombre: prod.nombre,
          descripcion: prod.descripcion,
          sku: prod.sku,
          unidadMedidaBase: prod.unidadMedidaBase,
          precioCostoPromedio: prod.precioCostoPromedio,
          stock: prod.stock,
          activo: prod.activo,
          idSucursal: this.idSucursal,
          idProveedor: prod.idProveedor || prod.proveedor
        });
      },
      error: (err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se cargó el producto', 'error');
      }
    });
  }

  editar() {
    if (this.form.valid) {
      const payload = {
        ...this.form.value,
        idSucursal: Number(this.form.value.idSucursal),
        idProveedor: Number(this.form.value.idProveedor),
        precioCostoPromedio: Number(this.form.value.precioCostoPromedio),
        stock: Number(this.form.value.stock),
        activo: this.form.value.activo,
        idUsuarioResponsable: +this.tokenService.getIDCuenta()
      };


      this.inventarioService.updateProduct(this.id, payload).subscribe({
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
