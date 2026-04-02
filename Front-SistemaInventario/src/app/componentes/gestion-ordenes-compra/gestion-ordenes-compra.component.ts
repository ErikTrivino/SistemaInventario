import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompraService } from '../../servicios/compra.service';
import { ProveedorService } from '../../servicios/proveedor.service';
import { InventarioService } from '../../servicios/inventario.service';
import { CompraHistoricoRespuestaDTO, InformacionProveedor, InformacionProducto } from '../../modelo/informacionObjeto';
import { OrdenCompraCrearDTO, DetalleCompraCrearDTO, OrdenCompraRecepcionDTO } from '../../modelo/crearObjetos';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-gestion-ordenes-compra',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginadorComponent],
  templateUrl: './gestion-ordenes-compra.component.html',
  styleUrl: './gestion-ordenes-compra.component.css'
})
export class GestionOrdenesCompraComponent implements OnInit {

  // Vista
  mostrandoFormulario: boolean = false;

  // Filtros Histórico
  filtroProveedor?: number;
  filtroProducto?: number;
  fechaDesde?: string;
  fechaHasta?: string;

  // Histórico y Paginación
  compras: CompraHistoricoRespuestaDTO[] = [];
  paginaActual: number = 0;
  totalElementos: number = 0;
  porPagina: number = 10;

  // Catalogos para select
  proveedores: InformacionProveedor[] = [];
  productos: InformacionProducto[] = [];

  // Estado Nueva Orden
  nuevaOrden: OrdenCompraCrearDTO = {
    idSucursalDestino: 1, // Por defecto o seleccionable
    idProveedor: 0,
    detalles: []
  };

  // Nombres en cache local para mostrar en tabla resumen
  nombresProductosMap: { [id: number]: string } = {};
  nombresProveedoresMap: { [id: number]: string } = {};

  constructor(
    private compraService: CompraService,
    private proveedorService: ProveedorService,
    private inventarioService: InventarioService
  ) { }

  ngOnInit(): void {
    this.cargarCatalogos();
    this.cargarHistorial();
  }

  cargarCatalogos(): void {
    this.proveedorService.listarTodos(0, 500).subscribe({
      next: (res) => {
        if (!res.error) {
          const arr = res.respuesta.content || [];
          const map = new Map<number, InformacionProveedor>();
          arr.forEach((p: InformacionProveedor) => map.set(p.id, p));
          this.proveedores = Array.from(map.values());
          this.proveedores.forEach(p => this.nombresProveedoresMap[p.id] = p.razonSocial);
        }
      }
    });

    this.inventarioService.getProducts(0, 1000).subscribe({
      next: (res) => {
        if (!res.error) {
          const arr = res.respuesta.content || [];
          const map = new Map<number, InformacionProducto>();
          arr.forEach((p: InformacionProducto) => map.set(p.id, p));
          this.productos = Array.from(map.values());
          this.productos.forEach(p => this.nombresProductosMap[p.id] = p.nombre);
        }
      }
    });
  }

  cargarHistorial(pagina: number = 0): void {
    this.paginaActual = pagina;
    this.compraService.obtenerHistorico(
      this.filtroProveedor,
      this.filtroProducto,
      this.fechaDesde,
      this.fechaHasta,
      this.paginaActual,
      this.porPagina
    ).subscribe({
      next: (res) => {
        if (!res.error && res.respuesta) {
          this.compras = res.respuesta.content;
          this.totalElementos = res.respuesta.totalElements;
        }
      },
      error: (err) => {
        Swal.fire('Error', err.error?.respuesta || 'Error al cargar historial.', 'error');
      }
    });
  }

  aplicarFiltros(): void {
    this.cargarHistorial(0);
  }

  limpiarFiltros(): void {
    this.filtroProveedor = undefined;
    this.filtroProducto = undefined;
    this.fechaDesde = undefined;
    this.fechaHasta = undefined;
    this.cargarHistorial(0);
  }

  cambiarPagina(nuevaPagina: number): void {
    this.cargarHistorial(nuevaPagina);
  }

  // --- NUEVA ORDEN ---

  abrirNuevaOrden(): void {
    this.mostrandoFormulario = true;
    this.nuevaOrden = {
      idSucursalDestino: 1, // Por ejemplo la sucursal del operario
      idProveedor: 0,
      detalles: []
    };
  }

  cancelarOrden(): void {
    this.mostrandoFormulario = false;
  }

  async agregarProductoModal() {
    let optionsHtml = '<option value="">Seleccione Producto</option>';
    this.productos.forEach(p => {
      optionsHtml += `<option value="${p.id}">${p.nombre} (ID: ${p.id})</option>`;
    });

    const { value: formValues } = await Swal.fire({
      title: 'Agregar Producto',
      html: `
        <select id="swal-producto" class="swal2-select mb-3" style="width:100%">${optionsHtml}</select>
        <input id="swal-cantidad" class="swal2-input mb-3" placeholder="Cantidad" type="number" min="1" step="0.01" style="width:100%">
        <input id="swal-precio" class="swal2-input mb-3" placeholder="Precio Unitario" type="number" min="0.01" step="0.01" style="width:100%">
        <input id="swal-descuento" class="swal2-input mb-3" placeholder="Descuento % (Opcional)" type="number" min="0" max="100" step="0.01" style="width:100%">
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Agregar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const prodId = (document.getElementById('swal-producto') as HTMLSelectElement).value;
        const cant = (document.getElementById('swal-cantidad') as HTMLInputElement).value;
        const precio = (document.getElementById('swal-precio') as HTMLInputElement).value;
        const desc = (document.getElementById('swal-descuento') as HTMLInputElement).value;

        if (!prodId || !cant || !precio) {
          Swal.showValidationMessage('El producto, la cantidad y el precio son obligatorios.');
          return false;
        }

        return {
          idProducto: Number(prodId),
          cantidad: Number(cant),
          precioUnitario: Number(precio),
          descuentoPorcentaje: desc ? Number(desc) : 0
        };
      }
    });

    if (formValues) {
      // Verificar si ya existe el producto en el array local para no duplicar filas
      const existeIndex = this.nuevaOrden.detalles.findIndex(d => d.idProducto === formValues.idProducto);
      if (existeIndex !== -1) {
        Swal.fire({
          icon: 'warning',
          title: 'Producto ya agregado',
          text: 'Este producto ya está en la lista de la nueva orden.'
        });
      } else {
        this.nuevaOrden.detalles.push(formValues);
        // SweetAlert toast exito
        Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Producto agregado', timer: 1500, showConfirmButton: false });
      }
    }
  }

  eliminarDetalle(index: number): void {
    this.nuevaOrden.detalles.splice(index, 1);
  }

  guardarOrden(): void {
    if (!this.nuevaOrden.idProveedor || this.nuevaOrden.idProveedor === 0) {
      Swal.fire('Atención', 'Debe seleccionar un proveedor.', 'warning');
      return;
    }
    if (this.nuevaOrden.detalles.length === 0) {
      Swal.fire('Atención', 'Debe agregar al menos un producto.', 'warning');
      return;
    }

    Swal.fire({
      title: 'Creando Orden...',
      allowOutsideClick: false,
      didOpen: () => { Swal.showLoading(); }
    });

    this.compraService.crearCompra(this.nuevaOrden).subscribe({
      next: (res) => {
        if (!res.error) {
          Swal.fire('¡Éxito!', 'La orden de compra ha sido creada con éxito.', 'success');
          this.mostrandoFormulario = false;
          this.cargarHistorial(0);
        } else {
          Swal.fire('Error', res.respuesta || 'Error al crear la orden.', 'error');
        }
      },
      error: (err) => {
        Swal.fire('Error', err.error?.respuesta || 'Error inesperado al crear la orden.', 'error');
      }
    });
  }

  // --- RECEPCION DE MERCADERIA ---

  async recibirProducto(compra: CompraHistoricoRespuestaDTO) {
    // Si la cantidad solicitada ya es igual a la recibida
    if (compra.cantidadSolicitada <= compra.cantidadRecibida) {
      Swal.fire('Aviso', 'Este producto ya fue recibido en su totalidad.', 'info');
      return;
    }

    const maxPosible = compra.cantidadSolicitada - compra.cantidadRecibida;

    const { value: formValues } = await Swal.fire({
      title: 'Recibir Producto',
      html: `
        <p class="mb-4">Ingresa la cantidad recibida y la sucursal de destino final (normalmente en la que te encuentras). Esta acción generará una entrada de inventario.</p>
        <div class="mb-3 text-left">
          <label class="block text-sm font-medium text-gray-700">Sucursal Destino ID:</label>
          <input id="rec-sucursal" class="swal2-input !mt-1 !w-full" type="number" min="1" value="1">
        </div>
        <div class="mb-3 text-left">
          <label class="block text-sm font-medium text-gray-700">Cantidad a Recibir (Pendiente: ${maxPosible}):</label>
          <input id="rec-cantidad" class="swal2-input !mt-1 !w-full" type="number" min="0.01" step="0.01" max="${maxPosible}">
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Confirmar Recepción',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const sucId = (document.getElementById('rec-sucursal') as HTMLInputElement).value;
        const cant = (document.getElementById('rec-cantidad') as HTMLInputElement).value;
        if (!sucId || !cant) {
          Swal.showValidationMessage('Revisar datos ingresados.');
          return false;
        }
        if (Number(cant) <= 0 || Number(cant) > maxPosible) {
          Swal.showValidationMessage('La cantidad no puede superar el pendiente (' + maxPosible + ').');
          return false;
        }
        return { suc: Number(sucId), cant: Number(cant) };
      }
    });

    if (formValues) {
      const dto: OrdenCompraRecepcionDTO = {
        idOrdenCompra: compra.idOrdenCompra,
        idSucursalDestino: formValues.suc,
        detallesRecibidos: [
          {
            idDetalle: compra.idDetalle,
            cantidadRecibida: formValues.cant
          }
        ]
      };

      Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => { Swal.showLoading(); } });

      this.compraService.recibirCompra(dto).subscribe({
        next: (res) => {
          if (!res.error) {
            Swal.fire('¡Recibido!', 'Se ha actualizado la cantidad y cargado el inventario.', 'success');
            this.cargarHistorial(this.paginaActual);
          } else {
            Swal.fire('Error', res.respuesta || 'Hubo un error al procesar.', 'error');
          }
        },
        error: (err) => Swal.fire('Error', err.error?.respuesta || 'Hubo un error al procesar.', 'error')
      });
    }
  }
}
