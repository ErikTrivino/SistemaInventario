import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CompraService {
  private apiUrl = `${environment.apiUrl}/api/compras`;

  constructor(private http: HttpClient) {}

  crearCompra(dto: any): Observable<MensajeDTO> {
    return this.http.post<MensajeDTO>(this.apiUrl, dto);
  }

  recibirCompra(dto: any): Observable<MensajeDTO> {
    return this.http.post<MensajeDTO>(`${this.apiUrl}/recepcion`, dto);
  }

  obtenerHistorico(idProveedor?: number, idProducto?: number, estado?: string, idSucursal?: number, fechaDesde?: string, fechaHasta?: string, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (idProveedor) params = params.set('idProveedor', idProveedor.toString());
    if (idProducto) params = params.set('idProducto', idProducto.toString());
    if (estado) params = params.set('estado', estado);
    if (idSucursal) params = params.set('idSucursal', idSucursal.toString());
    if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params = params.set('fechaHasta', fechaHasta);
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/historico`, { params });
  }
}
