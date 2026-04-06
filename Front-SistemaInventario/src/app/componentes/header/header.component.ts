import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TokenService } from '../../servicios/token.service';
import { UsuarioService } from '../../servicios/usuario.service';
import { UsuarioConsultaService } from '../../servicios/usuario-consulta.service';
import { TransferenciaService } from '../../servicios/transferencia.service';
import { CompraService } from '../../servicios/compra.service';
import { TableroService } from '../../servicios/tablero.service';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import { AlertaStockDTO } from '../../modelo/informacionObjeto';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {

  notificacionesCount = 0;
  transferenciasCount = 0;
  comprasCount = 0;
  stockAlertasCount = 0;
  productosBajoStock: AlertaStockDTO[] = [];
  sucursalId = 0;

  constructor(
    private tokenSvc: TokenService,
    private usuarioSvc: UsuarioService,
    private usuarioConsultaSvc: UsuarioConsultaService,
    private transferenciaSvc: TransferenciaService,
    private compraSvc: CompraService,
    private tableroSvc: TableroService
  ) { }

  ngOnInit(): void {
    if (this.tokenSvc.isLogged()) {
      const userId = Number(this.tokenSvc.getIDCuenta());
      if (userId) {
        this.usuarioConsultaSvc.consultarPorId(userId).subscribe({
          next: (res: MensajeDTO) => {
            this.sucursalId = res.respuesta?.sucursalAsignadaId;
            if (this.sucursalId) {
              this.verificarPendientes();
            }
          }
        });
      }
    }
  }

  verificarPendientes(): void {
    this.transferenciasCount = 0;
    this.comprasCount = 0;
    this.stockAlertasCount = 0;

    // 1. Transferencias EN_TRANSITO (entrantes)
    this.transferenciaSvc.getEntrantes(this.sucursalId, 'EN_TRANSITO').subscribe({
      next: (res: MensajeDTO) => {
        const data = res.respuesta;
        this.transferenciasCount += (data?.totalElements || data?.length || 0);
        this.actualizarTotal();
      }
    });

    // 2. Transferencias SOLICITADO (salientes por preparar)
    this.transferenciaSvc.getHistorico(this.sucursalId, 'SOLICITADO').subscribe({
      next: (res: MensajeDTO) => {
        const data = res.respuesta;
        this.transferenciasCount += (data?.totalElements || data?.length || 0);
        this.actualizarTotal();
      }
    });

    // 3. Compras PENDIENTE
    this.compraSvc.obtenerHistorico(undefined, undefined, 'PENDIENTE', this.sucursalId).subscribe({
      next: (res: MensajeDTO) => {
        const data = res.respuesta;
        this.comprasCount = (data?.totalElements || data?.length || 0);
        this.actualizarTotal();
      }
    });

    // 4. Bajo Stock Alerts
    this.tableroSvc.getAlertasStock(0, 5).subscribe({
      next: (res: MensajeDTO) => {
        const data = res.respuesta;
        this.stockAlertasCount = (data?.totalElements || data?.length || 0);
        this.productosBajoStock = data?.content || data || [];
        this.actualizarTotal();
      }
    });
  }

  actualizarTotal(): void {
    this.notificacionesCount = this.transferenciasCount + this.comprasCount + this.stockAlertasCount;
  }

  revisar(): void {
    if (this.notificacionesCount > 0) {
      let mensaje = 'Tienes las siguientes tareas pendientes: <br><br>';
      if (this.transferenciasCount > 0) {
        mensaje += `<b>Transferencias:</b> ${this.transferenciasCount}<br>`;
      }
      if (this.comprasCount > 0) {
        mensaje += `<b>Compras:</b> ${this.comprasCount}<br>`;
      }
      if (this.stockAlertasCount > 0) {
        mensaje += `<b>Bajo Stock:</b> ${this.stockAlertasCount}<br>`;
        const lista = this.productosBajoStock.slice(0, 5).map(p => 
          `- <b>${p.nombreProducto}</b> (Stock: ${p.stockActual} / Min: ${p.stockMinimo}) - Faltan: ${p.diferencia}`
        ).join('<br>');
        mensaje += `<small class="text-gray-500">${lista}${this.stockAlertasCount > 5 ? '<br>...y otros' : ''}</small><br>`;
      }

      Swal.fire({
        title: 'Tareas Pendientes',
        html: mensaje,
        icon: 'info',
        confirmButtonText: 'Entendido'
      }).then(() => {
        this.notificacionesCount = 0;
        this.transferenciasCount = 0;
        this.comprasCount = 0;
        this.stockAlertasCount = 0;
        this.productosBajoStock = [];
      });
    } else {
      Swal.fire({
        title: 'Sin Pendientes',
        text: 'No tienes tareas pendientes en esta sucursal.',
        icon: 'success',
        timer: 1500,
        showConfirmButton: false
      });
    }
  }

  public logout(): void {
    this.tokenSvc.logout();
  }

  public isLogged(): boolean {
    return this.tokenSvc.isLogged();
  }

}
