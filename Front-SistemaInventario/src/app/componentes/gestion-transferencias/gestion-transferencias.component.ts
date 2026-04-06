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
import { InformacionTransferencia, InformacionProducto, InformacionTransportistaDTO } from '../../modelo/informacionObjeto';
import { TransferenciaCrearDTO, TransferenciaPrepararDTO, TransferenciaConfirmarEnvioDTO, TransferenciaRecepcionDTO } from '../../modelo/crearObjetos';
import { MensajeDTO } from '../../modelo/mensaje-dto';

@Component({
  selector: 'app-gestion-transferencias',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginadorComponent],
  templateUrl: './gestion-transferencias.component.html',
  styleUrl: './gestion-transferencias.component.css'
})
export class GestionTransferenciasComponent implements OnInit {

  transferencias: InformacionTransferencia[] = [];

  // Paginación
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  // Filtros
  branchIdSeleccionada: number = 0;
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

  transportistas: InformacionTransportistaDTO[] = [];

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
        this.nombreSucursalUsuario = this.sucursalMap.get(this.sucursalUsuario) ?? '';
        this.cargar();
      },
      error: (err) => {
        console.error('Error cargando sucursales', err);
        this.cargar();
      }
    });
  }

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

    // Convertir fechas vacías a undefined para el servicio
    const desde = this.fechaDesde ? new Date(this.fechaDesde).toISOString() : undefined;
    const hasta = this.fechaHasta ? new Date(this.fechaHasta).toISOString() : undefined;
    const estado = this.estadoSeleccionado || undefined;

    this.svc.getHistorico(
      this.branchIdSeleccionada,
      estado,
      desde,
      hasta,
      this.paginaActual,
      this.tamanoPagina
    ).subscribe({
      next: (data: MensajeDTO) => {
        const respuesta = data.respuesta;
        if (respuesta?.content) {
          this.transferencias = respuesta.content;
          this.totalElementos = respuesta.totalElements;
          this.totalPaginas = respuesta.totalPages;
          this.paginaActual = respuesta.number;
        } else {
          this.transferencias = respuesta || [];
          this.totalElementos = this.transferencias.length;
          this.totalPaginas = 1;
          this.paginaActual = 0;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        Swal.fire('Error', 'No se pudieron cargar las transferencias', 'error');
      }
    });
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

    const { value: formValues } = await Swal.fire({
      title: 'Solicitar Transferencia',
      width: 520,
      html: `
  <div style="text-align: left; display: flex; flex-direction: column; gap: 16px; padding: 4px 2px;">

    <div style="display: flex; flex-direction: column; gap: 6px;">
      <label style="font-weight: 600; font-size: 13px;">ID Sucursal Origen</label>
      <input id="swal-orig" class="swal2-input" type="number" placeholder="Ej: 1" style="margin: 0; width: 100%;">
    </div>

    <div style="display: flex; flex-direction: column; gap: 6px;">
      <label style="font-weight: 600; font-size: 13px;">ID Sucursal Destino</label>
      <input id="swal-dest" class="swal2-input" type="number" placeholder="Ej: 2" style="margin: 0; width: 100%;">
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
        const orig = (document.getElementById('swal-orig') as HTMLInputElement).value;
        const dest = (document.getElementById('swal-dest') as HTMLInputElement).value;
        const prod = (document.getElementById('swal-prod-id') as HTMLInputElement).value;
        const cant = (document.getElementById('swal-cant') as HTMLInputElement).value;

        if (!orig || !dest) {
          Swal.showValidationMessage('Los IDs de sucursal son obligatorios');
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
        idSucursalOrigen: this.branchIdSeleccionada,
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
    // Buscar la cantidad solicitada del primer item como sugerencia
    const cantSol = t.items && t.items.length > 0 ? t.items[0].cantidadSolicitada : '';

    const { value: cantidad } = await Swal.fire({
      title: 'Preparar Transferencia',
      input: 'number',
      inputLabel: 'Cantidad Confirmada',
      inputValue: cantSol,
      showCancelButton: true,
      inputValidator: (value) => {
        if (!value || Number(value) < 1) {
          return 'Debes ingresar una cantidad válida';
        }
        return null; // Return null when valid! TypeScript wants either string | null
      }
    });

    if (cantidad) {
      const dto: TransferenciaPrepararDTO = {
        idTransferencia: t.idTransferencia,
        cantidadConfirmada: Number(cantidad)
      };

      this.svc.preparar(dto).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Transferencia preparada', 'success');
          this.cargar();
        },
        error: (err) => {
          console.error(err);
          Swal.fire('Error', 'Hubo un error', 'error');
        }
      });
    }
  }

  async enviarTransferencia(t: InformacionTransferencia) {
    const { value: formValues } = await Swal.fire({
      title: '¿Confirmar Envío?',
      html: `
        <div style="text-align: left; font-size: 14px; display: flex; flex-direction: column; gap: 12px;">
          <div style="background: #f9fafb; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">
            <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 6px;">
              <strong>ID:</strong> <span>#${t.idTransferencia}</span>
              <strong>Destino:</strong> <span>${this.getNombreSucursal(t.idSucursalDestino)}</span>
            </div>
          </div>

          <div style="display: flex; flex-direction: column; gap: 6px;">
            <label style="font-weight: 600; font-size: 13px;">Tiempo Estimado Entrega (días)</label>
            <input id="swal-envio-tiempo" class="swal2-input" type="number" placeholder="Ej: 3" style="margin: 0; width: 100%; height: 38px;">
          </div>

          <div style="display: flex; flex-direction: column; gap: 8px;">
            <label style="font-weight: 600; font-size: 13px;">Transportista</label>
            
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

            <div id="swal-trans-list" style="
              max-height: 150px;
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
                    });
                    this.classList.add('selected');
                    this.style.borderColor='#3b82f6';
                    this.style.background='#eff6ff';
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
            <input id="swal-id-transportista" type="hidden" value="">
          </div>
          
          <p style="margin-top: 5px; font-size: 12px; color: #6b7280; text-align: center;">Esta acción descontará el stock de la sucursal origen.</p>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, enviar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const tiempo = (document.getElementById('swal-envio-tiempo') as HTMLInputElement).value;
        const transportistaId = (document.getElementById('swal-id-transportista') as HTMLInputElement).value;

        if (!tiempo || Number(tiempo) <= 0) {
          Swal.showValidationMessage('Ingresa un tiempo estimado válido');
          return false;
        }
        if (!transportistaId) {
          Swal.showValidationMessage('Debes seleccionar un transportista');
          return false;
        }
        return {
          tiempoEstimadoEntrega: Number(tiempo),
          idTransportista: Number(transportistaId)
        };
      }
    });

    if (formValues) {
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

  trackById(_i: number, t: InformacionTransferencia) {
    return t.idTransferencia;
  }
}
