import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { InventarioService } from '../../servicios/inventario.service';
import { ProveedorService } from '../../servicios/proveedor.service';
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

  constructor(
    private fb: FormBuilder,
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService,
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
      idProveedor: ['', Validators.required],
      razonCambio: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  ngOnInit() {
    this.cargarProveedores();
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
          idProveedor: prod.proveedor // Map 'proveedor' ID to 'idProveedor' form control
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
        idSucursal: this.idSucursal,
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
