import { Component, OnInit } from '@angular/core';
import { InventarioService } from '../../servicios/inventario.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InformacionProducto } from '../../modelo/informacionObjeto';
import { ProveedorService } from '../../servicios/proveedor.service';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import { FormsModule } from '@angular/forms';
import { SucursalService } from '../../servicios/sucursal.service';

@Component({
  selector: 'app-gestion-producto',
  standalone: true,
  imports: [CommonModule, RouterModule, PaginadorComponent, FormsModule],
  templateUrl: './gestion-producto.component.html'
})
export class GestionProductoComponent implements OnInit {
  productos: InformacionProducto[] = [];
  seleccionados: InformacionProducto[] = [];
  textoBtnEliminar = '';

  // Sucursales
  sucursales: any[] = [];
  idSucursalSeleccionada: number | null = null;

  // Estado de paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  constructor(
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService,
    private sucursalService: SucursalService
  ) { }

  ngOnInit(): void {
    this.cargarSucursales();
    this.cargar();
  }

  cargarSucursales() {
    this.sucursalService.listar().subscribe({
      next: (data: MensajeDTO) => {
        this.sucursales = data.respuesta;
      },
      error: (err: any) => {
        console.error('Error al cargar sucursales:', err);
      }
    });
  }

  cargar() {
    const observable = this.idSucursalSeleccionada
      ? this.inventarioService.getInventoryByBranch(this.idSucursalSeleccionada, this.paginaActual + 1, this.tamanoPagina)
      : this.inventarioService.getProducts(this.paginaActual + 1, this.tamanoPagina);

    observable.subscribe({
      next: (data: MensajeDTO) => {


        let content = data.respuesta.content || data.respuesta;

        if (this.idSucursalSeleccionada && Array.isArray(content)) {
          this.productos = content.map((item: any) => ({
            id: item.idProducto,
            nombre: item.nombreProducto,
            sku: item.sku,
            descripcion: item.descripcion,
            unidadMedidaBase: item.unidadMedida,
            activo: item.activo,
            stockActual: item.stock,          // InventarioRespuestaDTO usa "stock"
            stockTotal: item.stock,
            precioCostoPromedio: item.precioCostoPromedio,
            idProveedor: item.idProveedor
          }));
        } else if (Array.isArray(content)) {
          this.productos = content as InformacionProducto[];
        } else {
          this.productos = [];
        }

        if (data.respuesta.content) {
          this.totalElementos = data.respuesta.totalElements;
          this.totalPaginas = data.respuesta.totalPages;
          this.paginaActual = data.respuesta.number;
        } else {
          this.totalElementos = this.productos.length;
          this.totalPaginas = 1;
        }

        // Cargar nombres de proveedores
        this.cargarProveedores();
      },
      error: (err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudieron cargar los productos', 'error');
      }
    });
  }

  cargarProveedores() {
    const proveedoresCache: { [key: number]: string } = {};

    for (let prod of this.productos) {
      if (prod.idProveedor) {
        // Si ya lo tenemos en el cache del ciclo, lo usamos directamente
        if (proveedoresCache[prod.idProveedor]) {
          prod.nombreProveedor = proveedoresCache[prod.idProveedor];
          continue;
        }

        this.proveedorService.consultarPorId(prod.idProveedor).subscribe({
          next: (prov: MensajeDTO) => {
            const nombre = prov.respuesta.razonSocial;
            prod.nombreProveedor = nombre;
            proveedoresCache[prod.idProveedor!] = nombre;
          },
          error: () => {
            prod.nombreProveedor = 'Proveedor no disponible';
          }
        });
      } else {
        prod.nombreProveedor = 'Sin asignar';
      }
    }
  }

  onCambioSucursal() {
    this.paginaActual = 0;
    this.cargar();
  }

  onCambioPagina(p: number) {
    this.paginaActual = p;
    this.cargar();
  }

  onCambioTamano(t: number) {
    this.tamanoPagina = t;
    this.paginaActual = 0;
    this.cargar();
  }

  seleccionar(p: InformacionProducto, sel: boolean) {
    if (sel) {
      if (!this.seleccionados.includes(p)) {
        this.seleccionados.push(p);
      }
    } else {
      const index = this.seleccionados.indexOf(p);
      if (index !== -1) {
        this.seleccionados.splice(index, 1);
      }
    }
    this.textoBtnEliminar = `${this.seleccionados.length} producto${this.seleccionados.length !== 1 ? 's' : ''}`;
  }

  confirmarEliminar() {
    Swal.fire({
      title: '¿Eliminar?',
      text: `Se eliminarán ${this.seleccionados.length} producto(s).`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (result.isConfirmed) this.eliminar();
    });
  }

  eliminar() {
    const promises = this.seleccionados.map(p =>
      this.inventarioService.deleteProduct(p.id!).toPromise()
    );

    Promise.all(promises)
      .then(() => {
        Swal.fire('Eliminado', 'Productos eliminados correctamente', 'success');
        this.cargar();
        this.seleccionados = [];
        this.textoBtnEliminar = '';
      })
      .catch((err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudo eliminar uno o más productos', 'error');
      });
  }

  trackById(_i: number, p: InformacionProducto) {
    return p.id;
  }
}
