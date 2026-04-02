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

  // Estado de paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  constructor(
    private inventarioService: InventarioService,
    private proveedorService: ProveedorService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    this.inventarioService.getProducts(this.paginaActual + 1, this.tamanoPagina).subscribe({
      next: (data: MensajeDTO) => {
        console.log('Datos de inventario recibidos:', data.respuesta);
        // Soporte para Page de Spring Boot o lista simple
        if (data.respuesta.content) {
          this.productos = data.respuesta.content;
          this.totalElementos = data.respuesta.totalElements;
          this.totalPaginas = data.respuesta.totalPages;
          this.paginaActual = data.respuesta.number;
        } else {
          this.productos = data.respuesta;
          this.totalElementos = this.productos.length;
          this.totalPaginas = 1;
        }

        // Buscar nombre del proveedor para cada producto de forma asíncrona
        for (let prod of this.productos) {
          if (prod.idProveedor) {
            this.proveedorService.consultarPorId(prod.idProveedor).subscribe({
              next: (prov: MensajeDTO) => {
                prod.nombreProveedor = prov.respuesta.nombre;
              },
              error: () => {
                prod.nombreProveedor = 'Proveedor no disponible';
              }
            });
          } else {
            prod.nombreProveedor = 'Sin asignar';
          }
        }
      },
      error: (err: any) => {
        console.error(err);
        Swal.fire('Error', 'No se pudieron cargar los productos', 'error');
      }
    });
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
