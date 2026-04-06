import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import { TransferenciaService } from '../../servicios/transferencia.service';
import { SucursalService } from '../../servicios/sucursal.service';
import { InventarioService } from '../../servicios/inventario.service';
import { UsuarioService } from '../../servicios/usuario.service';
import { UsuarioConsultaService } from '../../servicios/usuario-consulta.service';
import { TokenService } from '../../servicios/token.service';
import { TransportistaService } from '../../servicios/transportista.service';
import { InformacionTransferencia, InformacionProducto, InformacionTransportistaDTO, Page, ResumenDetalleDTO } from '../../modelo/informacionObjeto';
import { TransferenciaCrearDTO, TransferenciaPrepararDTO, TransferenciaConfirmarEnvioDTO, TransferenciaRecepcionDTO, TransferenciaConfirmarEnvioConCambiosDTO, ClasificacionRuta } from '../../modelo/crearObjetos';
import { TransferenciaCancelarDTO } from '../../modelo/editarObjeto';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-gestion-transferencias-solicitadas',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginadorComponent],
  templateUrl: './gestion-transferencias-solicitadas.component.html',
  styleUrl: './gestion-transferencias-solicitadas.component.css'
})
export class GestionTransferenciasSolicitadasComponent implements OnInit {

  transferencias: InformacionTransferencia[] = [];

  // Paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  // Filtros
  branchIdSeleccionada: number = 0;
  tipoRelacion: 'DESTINO' | 'ORIGEN' = 'DESTINO'; // DESTINO = Recibidas, ORIGEN = Enviadas
  estadoSeleccionado: string = '';
  fechaDesde: string = '';
  fechaHasta: string = '';

  loading = false;

  // Rol del usuario
  isAdmin = false;
  sucursalUsuario: number = 0;
  nombreSucursalUsuario: string = '';

  // Sucursales
  sucursales: { id: number; nombre: string }[] = [];
  private sucursalMap = new Map<number, string>();

  // Productos para el modal
  private todosLosProductos: InformacionProducto[] = [];

  constructor(
    private svc: TransferenciaService,
    private inventarioSvc: InventarioService,
    private sucursalSvc: SucursalService,
    private usuarioSvc: UsuarioService,
    private usuarioConsultaSvc: UsuarioConsultaService,
    private tokenSvc: TokenService,
    private transportistaSvc: TransportistaService
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.tokenSvc.getRol() === 'ADMIN';
    const idUsuario = Number(this.tokenSvc.getIDCuenta());

    if (idUsuario) {
      this.usuarioConsultaSvc.consultarPorId(idUsuario).subscribe({
        next: (data: MensajeDTO) => {
          const usuario = data.respuesta;
          this.sucursalUsuario = usuario?.sucursalAsignadaId ?? 0;
          this.branchIdSeleccionada = this.sucursalUsuario;

          // Cargar sucursales, transportistas y luego el histórico
          this.cargarSucursales();
          this.cargarTransportistas();
        },
        error: (err) => {
          console.error('Error obteniendo usuario', err);
          this.cargarSucursales();
        }
      });
    } else {
      this.cargarSucursales();
    }
  }

  cargarSucursales(): void {
    this.sucursalSvc.listar().subscribe({
      next: (data) => {
        this.sucursales = data.respuesta ?? [];
        this.sucursalMap = new Map(
          this.sucursales.map((s: any) => [s.id, s.nombre])
        );
        this.nombreSucursalUsuario = this.sucursalMap.get(this.branchIdSeleccionada) ?? '';
        this.cargar();
      },
      error: (err) => {
        console.error('Error cargando sucursales', err);
        this.cargar();
      }
    });
  }

  transportistas: InformacionTransportistaDTO[] = [];

  cargarTransportistas(): void {
    this.transportistaSvc.listarTransportistas().subscribe({
      next: (data) => {
        this.transportistas = data.respuesta ?? [];

      },
      error: (err) => {
        console.error('Error cargando transportistas', err);
      }
    });
  }

  getNombreSucursal(id: number): string {
    return this.sucursalMap.get(id) ?? `Suc. ${id}`;
  }

  // ========================
  // CARGA DE DATOS
  // ========================
  cargar(): void {
    this.loading = true;

    if (this.tipoRelacion === 'DESTINO') {
      this.svc.getPorSucursalDestino(
        this.branchIdSeleccionada,
        this.paginaActual,
        this.tamanoPagina
      ).subscribe({
        next: (data: MensajeDTO) => {

          this.procesarRespuesta(data);
        },
        error: (err) => this.manejarError(err)
      });
    } else {
      this.svc.getPorSucursalOrigen(
        this.branchIdSeleccionada,
        this.paginaActual,
        this.tamanoPagina
      ).subscribe({
        next: (data: MensajeDTO) => {

          this.procesarRespuesta(data);
        },
        error: (err) => this.manejarError(err)
      });
    }
  }

  private procesarRespuesta(data: MensajeDTO): void {
    const respuesta = data.respuesta;

    if (respuesta && typeof respuesta === 'object' && 'content' in respuesta) {

      const page = respuesta as Page<InformacionTransferencia>;
      this.transferencias = page.content;
      this.totalElementos = page.totalElements;
      this.totalPaginas = page.totalPages;
      this.paginaActual = page.number;
    } else {
      this.transferencias = (respuesta as InformacionTransferencia[]) || [];
      this.totalElementos = this.transferencias.length;
      this.totalPaginas = 1;
      this.paginaActual = 0;
    }
    this.loading = false;
  }

  private manejarError(err: any): void {
    console.error(err);
    this.loading = false;
    Swal.fire('Error', 'No se pudieron cargar las transferencias', 'error');
  }

  onCambioPagina(p: number) {
    this.paginaActual = p;
    this.cargar();
  }

  aplicarFiltros() {
    this.paginaActual = 0;
    this.cargar();
  }

  // ========================
  // ACCIONES
  // ========================

  async solicitarTransferencia() {
    // Cargar productos si aún no se han cargado
    if (this.todosLosProductos.length === 0) {
      try {
        const resp: MensajeDTO = await this.inventarioSvc.getProducts(0, 200, true).toPromise() as MensajeDTO;
        const data = resp?.respuesta;
        this.todosLosProductos = data?.content ?? data ?? [];
      } catch (e) {
        console.error('Error cargando productos', e);
      }
    }

    const buildProductRows = (lista: InformacionProducto[]) =>
      lista.map(p =>
        `<div
          class="prod-row"
          data-id="${p.id}"
          style="
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 8px 10px;
            border-radius: 8px;
            cursor: pointer;
            border: 2px solid transparent;
            transition: background 0.15s, border-color 0.15s;
          "
          onmouseover="this.style.background='#f0f4ff'"
          onmouseout="if(!this.classList.contains('selected'))this.style.background=''"
          onclick="
            document.querySelectorAll('.prod-row').forEach(r=>{
              r.classList.remove('selected');
              r.style.borderColor='transparent';
              r.style.background='';
            });
            this.classList.add('selected');
            this.style.borderColor='#4f46e5';
            this.style.background='#eef2ff';
            document.getElementById('swal-prod-id').value='${p.id}';
          "
        >
          <div style="
            width: 36px; height: 36px;
            border-radius: 50%;
            background: linear-gradient(135deg,#4f46e5,#7c3aed);
            display: flex; align-items: center; justify-content: center;
            color: #fff; font-weight: 700; font-size: 13px; flex-shrink: 0;
          ">${p.nombre.charAt(0).toUpperCase()}</div>
          <div style="flex: 1; min-width: 0;">
            <div style="font-weight: 600; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${p.nombre}</div>
            <div style="font-size: 11px; color: #6b7280;">SKU: ${p.sku} &nbsp;|&nbsp; ID: <strong>${p.id}</strong></div>
          </div>
        </div>`
      ).join('');

    // Filtrar sucursales para no mostrar la propia como origen
    const sucursalesOrigen = this.sucursales.filter(s => s.id !== this.sucursalUsuario);

    const { value: formValues } = await Swal.fire({
      title: 'Solicitar Transferencia',
      width: 520,
      html: `
  <div style="text-align: left; display: flex; flex-direction: column; gap: 16px; padding: 4px 2px;">

    <div style="display: flex; flex-direction: column; gap: 6px;">
      <label style="font-weight: 600; font-size: 13px;">Sucursal Origen</label>
      <select id="swal-orig" class="swal2-input" style="margin: 0; width: 100%;">
        <option value="" disabled selected>Seleccione sucursal origen</option>
        ${sucursalesOrigen.map(s => `<option value="${s.id}">${s.nombre} (ID: ${s.id})</option>`).join('')}
      </select>
    </div>

    <div style="display: flex; flex-direction: column; gap: 6px;">
      <label style="font-weight: 600; font-size: 13px;">Sucursal Destino (Tu Sucursal)</label>
      <input id="swal-dest-name" class="swal2-input" type="text" value="${this.nombreSucursalUsuario}" readonly 
        style="margin: 0; width: 100%; background-color: #f3f4f6; cursor: not-allowed; border: 1px solid #d1d5db;">
      <input id="swal-dest" type="hidden" value="${this.sucursalUsuario}">
    </div>

    <div style="display: flex; flex-direction: column; gap: 8px;">
      <label style="font-weight: 600; font-size: 13px;">Producto</label>

      <!-- Buscador -->
      <div style="position: relative;">
        <span style="position:absolute;left:10px;top:50%;transform:translateY(-50%);color:#9ca3af;">🔍</span>
        <input
          id="swal-buscar-prod"
          class="swal2-input"
          type="text"
          placeholder="Buscar por nombre o SKU..."
          style="margin: 0; width: 100%; padding-left: 32px;"
          oninput="
            const q = this.value.toLowerCase();
            document.querySelectorAll('.prod-row').forEach(r => {
              const txt = r.innerText.toLowerCase();
              r.style.display = txt.includes(q) ? '' : 'none';
            });
          "
        >
      </div>

      <!-- Lista de productos -->
      <div id="swal-prod-list" style="
        max-height: 200px;
        overflow-y: auto;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        padding: 6px;
        display: flex;
        flex-direction: column;
        gap: 4px;
        background: #fafafa;
      ">
        ${buildProductRows(this.todosLosProductos)}
      </div>

      <!-- ID oculto del producto seleccionado -->
      <input id="swal-prod-id" type="hidden" value="">

      <!-- Visualizador del producto seleccionado -->
      <div id="swal-prod-selected" style="font-size: 12px; color: #4f46e5; font-style: italic; min-height: 16px;"></div>
    </div>

    <div style="display: flex; flex-direction: column; gap: 6px;">
      <label style="font-weight: 600; font-size: 13px;">Cantidad</label>
      <input id="swal-cant" class="swal2-input" type="number" step="0.01" placeholder="Ej: 50" style="margin: 0; width: 100%;">
    </div>

  </div>
`,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Solicitar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const orig = (document.getElementById('swal-orig') as HTMLSelectElement).value;
        const dest = (document.getElementById('swal-dest') as HTMLInputElement).value;
        const prod = (document.getElementById('swal-prod-id') as HTMLInputElement).value;
        const cant = (document.getElementById('swal-cant') as HTMLInputElement).value;

        if (!orig || !dest) {
          Swal.showValidationMessage('La sucursal de origen es obligatoria');
          return false;
        }
        if (!prod) {
          Swal.showValidationMessage('Debes seleccionar un producto de la lista');
          return false;
        }
        if (!cant || Number(cant) <= 0) {
          Swal.showValidationMessage('Ingresa una cantidad válida');
          return false;
        }
        return {
          origen: Number(orig),
          destino: Number(dest),
          producto: Number(prod),
          cantidad: Number(cant)
        };
      }
    });

    if (formValues) {
      const dto: TransferenciaCrearDTO = {
        idSucursalOrigen: formValues.origen,
        idSucursalDestino: formValues.destino,
        items: [
          {
            idProducto: formValues.producto,
            cantidad: formValues.cantidad
          }
        ]
      };

      this.svc.solicitar(dto).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Transferencia solicitada correctamente', 'success');
          this.cargar();
        },
        error: (err) => {
          console.error(err);
          Swal.fire('Error', err.error?.mensaje || 'Hubo un error al solicitar', 'error');
        }
      });
    }
  }

  async prepararTransferencia(t: InformacionTransferencia) {
    const item = t.items && t.items.length > 0 ? t.items[0] : null;
    const cantSol = item ? (item.cantidadSolicitada || 0) : 0;
    const nombreProd = item?.nombreProducto || 'Producto desconocido';
    const skuProd = item?.skuProducto || 'N/A';
    const idProd = item?.idProducto || 'N/A';

    const { value: formValues } = await Swal.fire({
      title: 'Preparar Transferencia',
      width: 550,
      html: `
        <div style="text-align: left; display: flex; flex-direction: column; gap: 16px; padding: 4px 2px;">
          
          <!-- Información del Producto Solicitado -->
          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; margin-bottom: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px;">
              <div style="width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #4f46e5, #7c3aed); color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 20px; flex-shrink: 0;">
                ${nombreProd.charAt(0).toUpperCase()}
              </div>
              <div style="flex: 1; min-width: 0;">
                <h4 style="margin: 0; color: #1e293b; font-size: 16px; font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${nombreProd}</h4>
                <div style="display: flex; gap: 8px; margin-top: 2px;">
                   <span style="background: #eff6ff; color: #1e40af; font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600;">SKU: ${skuProd}</span>
                   <span style="background: #f1f5f9; color: #475569; font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600;">ID: ${idProd}</span>
                </div>
              </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
              <div style="background: white; padding: 10px 14px; border-radius: 10px; border: 1px solid #e2e8f0;">
                <p style="margin: 0; font-size: 10px; color: #64748b; text-transform: uppercase; font-weight: 700; letter-spacing: 0.5px;">Cant. Solicitada</p>
                <p style="margin: 2px 0 0 0; font-size: 20px; color: #4f46e5; font-weight: 800;">${cantSol}</p>
              </div>
              <div style="background: white; padding: 10px 14px; border-radius: 10px; border: 1px solid #e2e8f0;">
                <p style="margin: 0; font-size: 10px; color: #64748b; text-transform: uppercase; font-weight: 700; letter-spacing: 0.5px;">Sucursal Destino</p>
                <p style="margin: 2px 0 0 0; font-size: 14px; color: #1e293b; font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${this.getNombreSucursal(t.idSucursalDestino)}</p>
              </div>
            </div>
          </div>

          <div style="display: flex; flex-direction: column; gap: 6px;">
            <label style="font-weight: 600; font-size: 13px; color: #334155;">Cantidad Confirmada (A enviar)</label>
            <input id="swal-prep-cant" class="swal2-input" type="number" step="0.01" value="${cantSol}" style="margin: 0; width: 100%; border-radius: 8px;">
          </div>

          <div style="display: flex; flex-direction: column; gap: 6px;">
            <label style="font-weight: 600; font-size: 13px; color: #334155;">Clasificación de Ruta</label>
            <select id="swal-prep-clasif" class="swal2-input" style="margin: 0; width: 100%; border-radius: 8px;">
              <option value="PRIORIDAD">PRIORIDAD (Entrega rápida)</option>
              <option value="COSTO">COSTO (Ahorro transporte)</option>
              <option value="TIEMPO">TIEMPO (Optimización horario)</option>
            </select>
          </div>

          <div style="display: flex; flex-direction: column; gap: 6px;">
            <label style="font-weight: 600; font-size: 13px; color: #334155;">Lead Time Estándar (días)</label>
            <input id="swal-prep-lead" class="swal2-input" type="number" placeholder="Ej: 2" value="2" style="margin: 0; width: 100%; border-radius: 8px;">
          </div>
        </div>
      `,
      showCancelButton: true,
      confirmButtonText: 'Preparar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const cant = (document.getElementById('swal-prep-cant') as HTMLInputElement).value;
        const clasif = (document.getElementById('swal-prep-clasif') as HTMLSelectElement).value;
        const lead = (document.getElementById('swal-prep-lead') as HTMLInputElement).value;

        if (!cant || Number(cant) <= 0) {
          Swal.showValidationMessage('Ingresa una cantidad válida');
          return false;
        }
        if (!lead || Number(lead) < 1) {
          Swal.showValidationMessage('Ingresa un lead time válido');
          return false;
        }
        return {
          cantidadConfirmada: Number(cant),
          tipoClasificacion: clasif as ClasificacionRuta,
          leadTimeEstandar: Number(lead)
        };
      }
    });

    if (formValues) {
      const dto: TransferenciaPrepararDTO = {
        idTransferencia: t.idTransferencia,
        cantidadConfirmada: formValues.cantidadConfirmada,
        tipoClasificacion: formValues.tipoClasificacion,
        leadTimeEstandar: formValues.leadTimeEstandar
      };

      this.svc.preparar(dto).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Transferencia preparada y ruta establecida', 'success');
          this.cargar();
        },
        error: (err) => {
          console.error(err);
          Swal.fire('Error', err.error?.mensaje || 'Hubo un error al preparar la transferencia', 'error');
        }
      });
    }
  }

  async enviarTransferencia(t: InformacionTransferencia) {
    let stockActual = 0;
    let nombreProducto = 'Producto desconocido';

    // Obtener el stock actual en la sucursal de origen
    if (t.items && t.items.length > 0) {
      const idProducto = t.items[0].idProducto;
      try {
        const resp: any = await this.inventarioSvc.getProductByIdSucursal(t.idSucursalOrigen, idProducto).toPromise();
        if (resp && resp.respuesta) {
          stockActual = resp.respuesta.stock || 0;
          nombreProducto = resp.respuesta.nombre || `ID: ${idProducto}`;
        }
      } catch (e) {
        console.error('Error obteniendo stock del producto', e);
      }
    }

    const cantidadAEnviarOriginal = t.items && t.items.length > 0
      ? (t.items[0].cantidadConfirmada || t.items[0].cantidadSolicitada)
      : 0;

    const { value: formValues } = await Swal.fire({
      title: '¿Confirmar Envío?',
      html: `
        <div style="text-align: left; font-size: 14px;">
          <h4 style="margin-top: 0; color: #4f46e5; text-align: center;">Resumen de Transferencia</h4>
          <div style="background: #f9fafb; padding: 15px; border-radius: 8px; border: 1px solid #e5e7eb; margin-bottom: 15px;">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; align-items: center;">
              <div><strong>ID Transferencia:</strong></div><div>#${t.idTransferencia}</div>
              <div><strong>Destino:</strong></div><div>${this.getNombreSucursal(t.idSucursalDestino)}</div>
              <div><strong>Producto:</strong></div><div>${nombreProducto}</div>
              <div><strong>Cant. a Enviar:</strong></div>
              <div>
                <input id="swal-cant-enviar" type="number" class="swal2-input" style="margin: 0; width: 100%; height: 32px; font-size: 14px;" value="${cantidadAEnviarOriginal}">
              </div>
            </div>
          </div>
          
          <div id="swal-stock-warning" style="background: ${stockActual >= cantidadAEnviarOriginal ? '#ecfdf5' : '#fef2f2'}; padding: 15px; border-radius: 8px; border: 1px solid ${stockActual >= cantidadAEnviarOriginal ? '#a7f3d0' : '#fecaca'};">
             <div style="display: flex; justify-content: space-between; align-items: center;">
               <span style="font-weight: bold; color: ${stockActual >= cantidadAEnviarOriginal ? '#065f46' : '#991b1b'};">Stock de tu Sucursal:</span>
               <span style="font-size: 18px; font-weight: 800; color: ${stockActual >= cantidadAEnviarOriginal ? '#059669' : '#dc2626'};">${stockActual}</span>
             </div>
             <div id="swal-stock-msg-container">
             ${stockActual < cantidadAEnviarOriginal ? `<div style="color: #dc2626; font-size: 12px; margin-top: 8px; font-weight: 600;">⚠️ Tienes menos stock del que vas a enviar.</div>` : ''}
             </div>
          </div>

          <div style="margin-top: 15px; display: flex; flex-direction: column; gap: 6px;">
            <label style="font-weight: 600; font-size: 13px;">Tiempo Estimado Entrega (días)</label>
            <input id="swal-tiempo-estimado" class="swal2-input" type="number" placeholder="Ej: 3" style="margin: 0; width: 100%; height: 38px;">
          </div>

          <div style="margin-top: 15px; display: flex; flex-direction: column; gap: 8px;">
            <label style="font-weight: 600; font-size: 13px;">Transportista</label>
            
            <!-- Buscador Transportista -->
            <div style="position: relative;">
              <span style="position:absolute;left:10px;top:50%;transform:translateY(-50%);color:#9ca3af;z-index:10;">🔍</span>
              <input
                id="swal-buscar-trans"
                class="swal2-input"
                type="text"
                placeholder="Buscar transportista..."
                style="margin: 0; width: 100%; padding-left: 32px; height: 38px; font-size: 14px;"
                oninput="
                  const q = this.value.toLowerCase();
                  document.querySelectorAll('.trans-row').forEach(r => {
                    const txt = r.innerText.toLowerCase();
                    r.style.display = txt.includes(q) ? '' : 'none';
                  });
                "
              >
            </div>

            <!-- Lista de transportistas -->
            <div id="swal-trans-list" style="
              max-height: 140px;
              overflow-y: auto;
              border: 1px solid #e5e7eb;
              border-radius: 10px;
              padding: 4px;
              display: flex;
              flex-direction: column;
              gap: 2px;
              background: #fafafa;
            ">
              ${this.transportistas.filter(trans => trans.activo).map(trans => `
                <div
                  class="trans-row"
                  data-id="${trans.id}"
                  style="
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    padding: 8px 10px;
                    border-radius: 8px;
                    cursor: pointer;
                    border: 2px solid transparent;
                    transition: all 0.2s;
                  "
                  onmouseover="this.style.background='#f3f4f6'"
                  onmouseout="if(!this.classList.contains('selected'))this.style.background=''"
                  onclick="
                    document.querySelectorAll('.trans-row').forEach(r=>{
                      r.classList.remove('selected');
                      r.style.borderColor='transparent';
                      r.style.background='';
                      r.style.boxShadow='none';
                    });
                    this.classList.add('selected');
                    this.style.borderColor='#3b82f6';
                    this.style.background='#eff6ff';
                    this.style.boxShadow='0 2px 4px rgba(59, 130, 246, 0.1)';
                    document.getElementById('swal-id-transportista').value='${trans.id}';
                  "
                >
                  <div style="
                    width: 32px; height: 32px;
                    border-radius: 8px;
                    background: #dbeafe;
                    display: flex; align-items: center; justify-content: center;
                    color: #1e40af; font-weight: 700; font-size: 12px;
                  ">${trans.nombre.charAt(0).toUpperCase()}</div>
                  <div style="flex: 1; min-width: 0;">
                    <div style="font-weight: 600; font-size: 13px; color: #1f2937; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${trans.nombre}</div>
                    <div style="font-size: 11px; color: #6b7280;">NIT: ${trans.nit}</div>
                  </div>
                </div>
              `).join('')}
            </div>

            <!-- ID oculto del transportista seleccionado -->
            <input id="swal-id-transportista" type="hidden" value="">
          </div>

          <p style="margin-top: 15px; font-size: 13px; color: #6b7280; text-align: center;">Esta acción descontará el stock de tu inventario inmediatamente.</p>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, enviar',
      cancelButtonText: 'Cancelar',
      didOpen: () => {
        const inputCant = document.getElementById('swal-cant-enviar') as HTMLInputElement;
        const warningDiv = document.getElementById('swal-stock-warning') as HTMLElement;
        const msgContainer = document.getElementById('swal-stock-msg-container') as HTMLElement;

        inputCant.addEventListener('input', () => {
          const nuevaCant = Number(inputCant.value);
          if (stockActual >= nuevaCant) {
            warningDiv.style.background = '#ecfdf5';
            warningDiv.style.borderColor = '#a7f3d0';
            warningDiv.querySelector('span:first-child')!.setAttribute('style', 'font-weight: bold; color: #065f46;');
            warningDiv.querySelector('span:last-child')!.setAttribute('style', 'font-size: 18px; font-weight: 800; color: #059669;');
            msgContainer.innerHTML = '';
          } else {
            warningDiv.style.background = '#fef2f2';
            warningDiv.style.borderColor = '#fecaca';
            warningDiv.querySelector('span:first-child')!.setAttribute('style', 'font-weight: bold; color: #991b1b;');
            warningDiv.querySelector('span:last-child')!.setAttribute('style', 'font-size: 18px; font-weight: 800; color: #dc2626;');
            msgContainer.innerHTML = '<div style="color: #dc2626; font-size: 12px; margin-top: 8px; font-weight: 600;">⚠️ Tienes menos stock del que vas a enviar.</div>';
          }
        });
      },
      preConfirm: () => {
        const cant = (document.getElementById('swal-cant-enviar') as HTMLInputElement).value;
        const tiempo = (document.getElementById('swal-tiempo-estimado') as HTMLInputElement).value;
        const transportistaId = (document.getElementById('swal-id-transportista') as HTMLInputElement).value;

        if (!cant || Number(cant) <= 0) {
          Swal.showValidationMessage('Ingresa una cantidad válida');
          return false;
        }

        if (!tiempo || Number(tiempo) <= 0) {
          Swal.showValidationMessage('Ingresa un tiempo estimado válido');
          return false;
        }

        if (!transportistaId) {
          Swal.showValidationMessage('Debes seleccionar un transportista');
          return false;
        }

        return {
          cantidadAEnviarFinal: Number(cant),
          tiempoEstimadoEntrega: Number(tiempo),
          idTransportista: Number(transportistaId)
        };
      }
    });

    if (formValues) {
      if (formValues.cantidadAEnviarFinal !== cantidadAEnviarOriginal) {
        // Enviar con cambios en la cantidad
        const dtoCambios: TransferenciaConfirmarEnvioConCambiosDTO = {
          idTransferencia: t.idTransferencia,
          StockAceptadoEnvio: formValues.cantidadAEnviarFinal,
          tiempoEstimadoEntrega: formValues.tiempoEstimadoEntrega,
          idTransportista: formValues.idTransportista
        };
        this.svc.enviarConCambios(dtoCambios).subscribe({
          next: () => {
            Swal.fire('Enviado', 'La transferencia está en camino con la nueva cantidad', 'success');
            this.cargar();
          },
          error: (err) => {
            console.error(err);
            Swal.fire('Error', err.error?.mensaje || 'No se pudo enviar', 'error');
          }
        });
      } else {
        // Enviar normal sin cambios
        const dto: TransferenciaConfirmarEnvioDTO = {
          idTransferencia: t.idTransferencia,
          tiempoEstimadoEntrega: formValues.tiempoEstimadoEntrega,
          idTransportista: formValues.idTransportista
        };
        this.svc.enviar(dto).subscribe({
          next: () => {
            Swal.fire('Enviado', 'La transferencia está en camino', 'success');
            this.cargar();
          },
          error: (err) => {
            console.error(err);
            Swal.fire('Error', err.error?.mensaje || 'No se pudo enviar', 'error');
          }
        });
      }
    }
  }

  async recibirTransferencia(t: InformacionTransferencia) {
    const cantConf = t.items && t.items.length > 0 ? t.items[0].cantidadConfirmada : '';

    const { value: cantidad } = await Swal.fire({
      title: 'Recibir Transferencia',
      input: 'number',
      inputLabel: 'Cantidad Recibida',
      inputValue: cantConf,
      showCancelButton: true,
      inputValidator: (value) => {
        if (!value || Number(value) < 0) {
          return 'Debes ingresar una cantidad válida';
        }
        return null;
      }
    });

    if (cantidad) {
      const dto: TransferenciaRecepcionDTO = {
        idTransferencia: t.idTransferencia,
        cantidadRecibida: Number(cantidad)
      };

      this.svc.recibir(dto).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Transferencia recibida (stock sumado en destino)', 'success');
          this.cargar();
        },
        error: (err) => {
          console.error(err);
          Swal.fire('Error', 'Hubo un error al recibir', 'error');
        }
      });
    }
  }

  async cancelarTransferencia(t: InformacionTransferencia) {
    const { isConfirmed } = await Swal.fire({
      title: '¿Estás seguro?',
      text: `¿Deseas cancelar la transferencia #${t.idTransferencia}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No, mantener',
      confirmButtonColor: '#d33'
    });

    if (isConfirmed) {
      const dto: TransferenciaCancelarDTO = {
        idTransferencia: t.idTransferencia
      };

      this.svc.cancelar(dto).subscribe({
        next: () => {
          Swal.fire('Cancelada', 'La transferencia ha sido cancelada correctamente', 'success');
          this.cargar();
        },
        error: (err) => {
          console.error(err);
          Swal.fire('Error', err.error?.mensaje || 'No se pudo cancelar la transferencia', 'error');
        }
      });
    }
  }

  trackById(_i: number, t: InformacionTransferencia) {
    return t.idTransferencia;
  }
}

