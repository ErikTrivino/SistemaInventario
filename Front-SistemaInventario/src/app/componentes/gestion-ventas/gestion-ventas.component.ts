import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VentaService } from '../../servicios/venta.service';
import { InventarioService } from '../../servicios/inventario.service';
import { TokenService } from '../../servicios/token.service';
import { SucursalService } from '../../servicios/sucursal.service';
import { UsuarioService } from '../../servicios/usuario.service';
import { UsuarioConsultaService } from '../../servicios/usuario-consulta.service';
import { VentaCrearDTO } from '../../modelo/crearObjetos';
import { VentaInformacionDTO } from '../../modelo/informacionObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-gestion-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginadorComponent],
  templateUrl: './gestion-ventas.component.html',
  styleUrl: './gestion-ventas.component.css'
})
export class GestionVentasComponent implements OnInit {

  // State
  activeTab: 'new-sale' | 'history' = 'new-sale';
  isLoading = false;

  // Catalog State (New Sale)
  catalog: any[] = [];
  filteredCatalog: any[] = [];
  searchTerm: string = '';
  currentSaleItems: any[] = [];
  userSucursalId: number = 0;
  userId: number = 0;

  // Pagination for Catalog
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;
  filtroActivo: any = true;

  // History State
  salesHistory: VentaInformacionDTO[] = [];
  branches: any[] = [];
  selectedBranchId: number | null = null;
  startDate: string = '';
  endDate: string = '';

  constructor(
    private ventaService: VentaService,
    private inventarioService: InventarioService,
    private tokenService: TokenService,
    private sucursalService: SucursalService,
    private usuarioService: UsuarioService,
    private usuarioConsultaService: UsuarioConsultaService
  ) { }

  ngOnInit(): void {
    const id = this.tokenService.getIDCuenta();
    if (id) {
      this.userId = parseInt(id);
      this.loadUserData();
    }
    this.loadBranches();
  }

  loadUserData() {
    if (this.userId) {
      this.usuarioConsultaService.consultarPorId(this.userId).subscribe({
        next: (res) => {
          this.userSucursalId = res.respuesta.sucursalAsignadaId;
          if (this.activeTab === 'new-sale') {
            this.loadCatalog();
          }

          this.selectedBranchId = this.userSucursalId;

        },
        error: (err) => console.error('Error loading user data', err)
      });
    }
  }

  loadBranches() {
    this.sucursalService.listar().subscribe({
      next: (res) => this.branches = res.respuesta,
      error: (err) => console.error('Error loading branches', err)
    });
  }

  loadCatalog() {
    if (!this.userSucursalId) return;
    this.isLoading = true;
    this.inventarioService.getCatalogo(this.userSucursalId, this.paginaActual + 1, this.tamanoPagina, this.filtroActivo).subscribe({
      next: (data: MensajeDTO) => {
        let content = data.respuesta.content || data.respuesta;


        if (Array.isArray(content)) {
          this.catalog = content.map((item: any) => ({
            id: item.idProducto || item.id,
            nombre: item.nombreProducto || item.nombre,
            sku: item.sku,
            cantidad: item.stock !== undefined ? item.stock : (item.cantidad || 0),
            precioVenta: item.precioVenta || item.precioCostoPromedio || 0
          }));
          this.filterCatalog();
        } else {
          this.catalog = [];
          this.filteredCatalog = [];
        }

        if (data.respuesta.content) {
          this.totalElementos = data.respuesta.totalElements;
          this.totalPaginas = data.respuesta.totalPages;
          this.paginaActual = data.respuesta.number;
        } else {
          this.totalElementos = this.catalog.length;
          this.totalPaginas = 1;
        }

        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading catalog', err);
        this.isLoading = false;
      }
    });
  }

  onCambioPagina(p: number) {
    this.paginaActual = p;
    this.loadCatalog();
  }

  onCambioTamano(t: number) {
    this.tamanoPagina = t;
    this.paginaActual = 0;
    this.loadCatalog();
  }

  filterCatalog() {
    if (!this.searchTerm) {
      this.filteredCatalog = this.catalog;
    } else {
      this.filteredCatalog = this.catalog.filter(p =>
        p.nombre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.sku.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }
  }

  addItemToSale(product: any) {
    const existing = this.currentSaleItems.find(item => item.idProducto === product.id);
    const quantityToAdd = 1;

    if (existing) {
      this.validateAndAdd(product, existing.cantidad + quantityToAdd);
    } else {
      this.validateAndAdd(product, quantityToAdd);
    }
  }

  validateAndAdd(product: any, totalQuantity: number) {
    this.ventaService.checkStock(product.id, this.userSucursalId, totalQuantity).subscribe({
      next: (res) => {
        // En base a la estructura de MensajeDTO, res.error nos dirá si falló
        if (res.error) {
          Swal.fire('Atención', res.respuesta, 'warning');
        } else {
          const index = this.currentSaleItems.findIndex(item => item.idProducto === product.id);
          if (index !== -1) {
            this.currentSaleItems[index].cantidad = totalQuantity;
            const discount = this.currentSaleItems[index].descuentoPorcentaje || 0;
            this.currentSaleItems[index].subtotal = (totalQuantity * product.precioVenta) * (1 - discount / 100);
          } else {
            this.currentSaleItems.push({
              idProducto: product.id,
              nombre: product.nombre,
              precioUnitario: product.precioVenta,
              cantidad: totalQuantity,
              descuentoPorcentaje: 0,
              subtotal: totalQuantity * product.precioVenta
            });
          }
        }
      },
      error: (err) => Swal.fire('Error', 'No se pudo validar el stock', 'error')
    });
  }

  removeItem(index: number) {
    this.currentSaleItems.splice(index, 1);
  }

  updateQuantity(index: number, newQty: number) {
    if (newQty <= 0) {
      this.removeItem(index);
      return;
    }
    const item = this.currentSaleItems[index];
    // We need the product info to re-validate. In a real app we might store it or fetch it.
    // Here we assume item has what's needed.
    this.ventaService.checkStock(item.idProducto, this.userSucursalId, newQty).subscribe({
      next: (res) => {
        if (res.error) {
          Swal.fire('Atención', res.respuesta, 'warning');
        } else {
          item.cantidad = newQty;
          const discount = item.descuentoPorcentaje || 0;
          item.subtotal = (newQty * item.precioUnitario) * (1 - discount / 100);
        }

      }
    });
  }

  get totalSale(): number {
    return this.currentSaleItems.reduce((acc, item) => acc + item.subtotal, 0);
  }

  updateDiscount(index: number, newDiscount: number) {
    if (newDiscount < 0) newDiscount = 0;
    if (newDiscount > 100) newDiscount = 100;
    
    const item = this.currentSaleItems[index];
    item.descuentoPorcentaje = newDiscount;
    item.subtotal = (item.cantidad * item.precioUnitario) * (1 - newDiscount / 100);
  }

  confirmSale() {
    if (this.currentSaleItems.length === 0) {
      Swal.fire('Carrito Vacío', 'Agrega productos a la venta', 'info');
      return;
    }

    const dto: VentaCrearDTO = {
      idSucursal: this.userSucursalId,
      idResponsable: this.userId,
      detalles: this.currentSaleItems.map(item => ({
        idProducto: item.idProducto,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario,
        descuentoPorcentaje: item.descuentoPorcentaje
      }))
    };

    Swal.fire({
      title: '¿Confirmar Venta?',
      text: `Total a pagar: $${this.totalSale.toLocaleString()}`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, registrar venta',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#2563eb'
    }).then((result) => {
      if (result.isConfirmed) {
        this.isLoading = true;
        this.ventaService.createSale(dto).subscribe({
          next: (res) => {
            Swal.fire('¡Venta Registrada!', 'La venta se ha procesado con éxito.', 'success');
            const ventId = res.respuesta.id;
            this.currentSaleItems = [];
            this.loadCatalog();
            this.isLoading = false;

            if (ventId) {
              this.downloadReceipt(ventId);
            }
          },
          error: (err) => {
            Swal.fire('Error', err.error.respuesta || 'No se pudo registrar la venta', 'error');
            this.isLoading = false;
          }
        });
      }
    });
  }

  loadSalesHistory() {
    if (!this.selectedBranchId) return;
    this.isLoading = true;

    if (this.startDate && this.endDate) {
      this.ventaService.getSalesByDateRange(this.startDate, this.endDate).subscribe({
        next: (res) => {
          if (!res.error && res.respuesta) {
            this.salesHistory = (res.respuesta.content || res.respuesta || []) as VentaInformacionDTO[];
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error history dates', err);
          this.isLoading = false;
        }
      });
    } else {
      this.ventaService.getSalesByBranch(this.selectedBranchId).subscribe({
        next: (res) => {
          if (!res.error && res.respuesta) {
            this.salesHistory = (res.respuesta.content || res.respuesta || []) as VentaInformacionDTO[];
          }
          this.isLoading = false;
          console.log(this.salesHistory);
        },
        error: (err) => {
          console.error('Error history branch', err);
          this.isLoading = false;
        }
      });
    }
  }

  downloadReceipt(saleId: number) {
    this.ventaService.obtenerComprobanteVenta(saleId).subscribe({
      next: (res) => {
        const base64String = res.respuesta;
        const binaryString = window.atob(base64String);
        const binaryLen = binaryString.length;
        const bytes = new Uint8Array(binaryLen);
        for (let i = 0; i < binaryLen; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }
        const blob = new Blob([bytes], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Comprobante_Venta_${saleId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => Swal.fire('Error', 'No se pudo descargar el comprobante', 'error')
    });
  }

  switchTab(tab: 'new-sale' | 'history') {
    this.activeTab = tab;
    if (tab === 'history') {
      this.loadSalesHistory();
    } else {
      this.loadCatalog();
    }
  }
}
