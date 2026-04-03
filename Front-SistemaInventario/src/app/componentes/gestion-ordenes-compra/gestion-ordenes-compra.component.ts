import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompraService } from '../../servicios/compra.service';
import { ProveedorService } from '../../servicios/proveedor.service';
import { InventarioService } from '../../servicios/inventario.service';
import { SucursalService } from '../../servicios/sucursal.service';
import { CompraHistoricoRespuestaDTO, InformacionProveedor, InformacionProducto } from '../../modelo/informacionObjeto';
import { OrdenCompraCrearDTO, DetalleCompraCrearDTO, OrdenCompraRecepcionDTO } from '../../modelo/crearObjetos';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import Swal from 'sweetalert2';

export interface CompraDetalleLocal extends CompraHistoricoRespuestaDTO {
  cantidadRecibiendo?: number;
}

export interface OrdenAgrupada {
  idOrdenCompra: number;
  fechaCompra: Date;
  nombreProveedor: string;
  idProveedor: number;
  estado: string;
  detalles: CompraDetalleLocal[];
  expandida?: boolean;
}

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
  filtroSucursal?: number;
  filtroEstado?: string;
  fechaDesde?: string;
  fechaHasta?: string;

  // Histórico y Paginación
  compras: CompraHistoricoRespuestaDTO[] = [];
  ordenesAgrupadas: OrdenAgrupada[] = [];
  paginaActual: number = 0;
  totalElementos: number = 0;
  totalPaginas: number = 0;
  porPagina: number = 10;

  // Catalogos para select
  proveedores: InformacionProveedor[] = [];
  productos: InformacionProducto[] = [];
  sucursales: any[] = [];

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
    private inventarioService: InventarioService,
    private sucursalService: SucursalService
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

    this.sucursalService.listar().subscribe({
      next: (res: any) => {
        if (!res.error) {
          this.sucursales = res.respuesta;
        }
      }
    });
  }

  cargarHistorial(pagina: number = 0): void {
    this.paginaActual = pagina;
    this.compraService.obtenerHistorico(
      this.filtroProveedor,
      this.filtroProducto,
      this.filtroEstado,
      this.filtroSucursal,
      this.fechaDesde,
      this.fechaHasta,
      this.paginaActual,
      this.porPagina
    ).subscribe({
      next: (res) => {
        console.log('Datos recibidos del servicio (Historial):', res);
        if (!res.error && res.respuesta) {
          const flatResult: CompraHistoricoRespuestaDTO[] = res.respuesta.content;
          this.compras = flatResult; // Mantener por si acaso, pero usaremos agrupadas

          // Lógica de agrupar por ID Orden
          const map = new Map<number, OrdenAgrupada>();
          flatResult.forEach(item => {
            if (!map.has(item.idOrdenCompra)) {
              map.set(item.idOrdenCompra, {
                idOrdenCompra: item.idOrdenCompra,
                fechaCompra: item.fechaCompra,
                nombreProveedor: item.nombreProveedor,
                idProveedor: item.idProveedor,
                estado: item.estado, // Mantenemos el estado de la primera fila como general
                detalles: [],
                expandida: false
              });
            }
            // Inicializar cantidad a recibir en 0
            const detalleLocal: CompraDetalleLocal = { 
              ...item,
              cantidadRecibiendo: 0 
            };
            map.get(item.idOrdenCompra)?.detalles.push(detalleLocal);
          });

          this.ordenesAgrupadas = Array.from(map.values());
          this.totalElementos = res.respuesta.totalElements;
          this.totalPaginas = res.respuesta.totalPages;
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
    this.filtroSucursal = undefined;
    this.filtroEstado = undefined;
    this.fechaDesde = undefined;
    this.fechaHasta = undefined;
    this.cargarHistorial(0);
  }

  cambiarPagina(nuevaPagina: number): void {
    this.cargarHistorial(nuevaPagina);
  }

  toggleOrden(orden: OrdenAgrupada): void {
    orden.expandida = !orden.expandida;
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

  async recibirOrden(orden: OrdenAgrupada) {
    // Filtrar solo los detalles que tienen cantidad para recibir
    const detallesParaRecibir = orden.detalles.filter(d => (d.cantidadRecibiendo || 0) > 0);

    if (detallesParaRecibir.length === 0) {
      Swal.fire('Atención', 'Debe ingresar una cantidad a recibir en al menos un producto.', 'warning');
      return;
    }

    // Validar que no se reciba más de lo pendiente (opcional, pero recomendado)
    for (const det of detallesParaRecibir) {
      const pendiente = det.cantidadSolicitada - det.cantidadRecibida;
      if ((det.cantidadRecibiendo || 0) > pendiente) {
        Swal.fire('Atención', `La cantidad a recibir de ${det.nombreProducto} supera lo pendiente (${pendiente}).`, 'warning');
        return;
      }
    }

    const { value: sucursalId } = await Swal.fire({
      title: 'Confirmar Recepción',
      text: 'Seleccione la sucursal de destino para el ingreso al inventario:',
      input: 'number',
      inputValue: 1, // Por defecto
      showCancelButton: true,
      confirmButtonText: 'Confirmar Recepción',
      cancelButtonText: 'Cancelar',
      inputValidator: (value) => {
        if (!value || Number(value) < 1) {
          return 'Debe ingresar un ID de sucursal válido';
        }
        return null;
      }
    });

    if (sucursalId) {
      const dto: OrdenCompraRecepcionDTO = {
        idOrdenCompra: orden.idOrdenCompra,
        idSucursalDestino: Number(sucursalId),
        detallesRecibidos: detallesParaRecibir.map(d => ({
          idDetalle: d.idDetalle,
          cantidadRecibida: d.cantidadRecibiendo || 0
        }))
      };

      Swal.fire({
        title: 'Procesando...',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
      });

      this.compraService.recibirCompra(dto).subscribe({
        next: (res) => {
          if (!res.error) {
            Swal.fire('¡Éxito!', 'La recepción ha sido registrada correctamente.', 'success');
            this.cargarHistorial(this.paginaActual);
          } else {
            Swal.fire('Error', res.respuesta || 'Error al procesar la recepción.', 'error');
          }
        },
        error: (err) => {
          Swal.fire('Error', err.error?.respuesta || 'Error inesperado al procesar la recepción.', 'error');
        }
      });
    }
  }
}
