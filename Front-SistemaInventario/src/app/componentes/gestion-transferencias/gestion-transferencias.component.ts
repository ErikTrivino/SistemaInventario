import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { PaginadorComponent } from '../comun/paginador/paginador.component';
import { TransferenciaService } from '../../servicios/transferencia.service';
import { InformacionTransferencia } from '../../modelo/informacionObjeto';
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
  branchIdSeleccionada: number = 1; // Sucursal por defecto
  estadoSeleccionado: string = '';
  fechaDesde: string = '';
  fechaHasta: string = '';

  loading = false;

  constructor(private svc: TransferenciaService) {}

  ngOnInit(): void {
    this.cargar();
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
    const { value: formValues } = await Swal.fire({
      title: 'Solicitar Transferencia',
      html: `
        <div class="swal2-form-group" style="text-align: left;">
          <label>ID Surcursal Destino</label>
          <input id="swal-dest" class="swal2-input" type="number" placeholder="Ej: 2">
          
          <label>ID Producto</label>
          <input id="swal-prod" class="swal2-input" type="number" placeholder="Ej: 101">
          
          <label>Cantidad</label>
          <input id="swal-cant" class="swal2-input" type="number" step="0.01" placeholder="Ej: 50">
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Solicitar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const dest = (document.getElementById('swal-dest') as HTMLInputElement).value;
        const prod = (document.getElementById('swal-prod') as HTMLInputElement).value;
        const cant = (document.getElementById('swal-cant') as HTMLInputElement).value;
        
        if (!dest || !prod || !cant) {
          Swal.showValidationMessage('Todos los campos son obligatorios');
          return false;
        }
        return { 
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

  enviarTransferencia(t: InformacionTransferencia) {
    Swal.fire({
      title: '¿Confirmar Envío?',
      text: "Esto descontará el stock de la sucursal origen.",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, enviar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        const dto: TransferenciaConfirmarEnvioDTO = {
          idTransferencia: t.idTransferencia
        };
        this.svc.enviar(dto).subscribe({
          next: () => {
            Swal.fire('Enviado', 'La transferencia está en camino', 'success');
            this.cargar();
          },
          error: (err) => {
             console.error(err);
             Swal.fire('Error', 'No se pudo enviar', 'error');
          }
        });
      }
    });
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
