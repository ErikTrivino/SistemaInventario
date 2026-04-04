import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private apiUrl = `${environment.apiUrl}/api/reportes`;

  constructor(private http: HttpClient) {}

  generarReporteVentas(inicio: string, fin: string, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin)
      .set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/ventas`, { params });
  }

  generarReporteInventario(idSucursal?: number, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (idSucursal) params = params.set('idSucursal', idSucursal.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/inventario`, { params });
  }

  generarReporteTransferencias(inicio: string, fin: string, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin)
      .set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/transferencias`, { params });
  }

  generarComparativoAnual(anio: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/comparativo/${anio}`);
  }

  generarAnalisisRotacion(mes: number, anio: number, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams()
      .set('mes', mes.toString())
      .set('anio', anio.toString())
      .set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/rotacion`, { params });
  }

  obtenerBase64ReporteInventario(idSucursal?: number): Observable<MensajeDTO<string>> {
    let params = new HttpParams();
    if (idSucursal) params = params.set('idSucursal', idSucursal.toString());
    return this.http.get<MensajeDTO<string>>(`${this.apiUrl}/inventario/pdf`, { params });
  }

  obtenerBase64ReporteVentas(inicio: string, fin: string): Observable<MensajeDTO<string>> {
    const params = new HttpParams().set('inicio', inicio).set('fin', fin);
    return this.http.get<MensajeDTO<string>>(`${this.apiUrl}/ventas/pdf`, { params });
  }

  obtenerBase64ReporteTransferencias(inicio: string, fin: string): Observable<MensajeDTO<string>> {
    const params = new HttpParams().set('inicio', inicio).set('fin', fin);
    return this.http.get<MensajeDTO<string>>(`${this.apiUrl}/transferencias/pdf`, { params });
  }

  obtenerBase64ComparativoAnual(anio: number): Observable<MensajeDTO<string>> {
    return this.http.get<MensajeDTO<string>>(`${this.apiUrl}/comparativo/${anio}/pdf`);
  }

  obtenerBase64AnalisisRotacion(mes: number, anio: number): Observable<MensajeDTO<string>> {
    const params = new HttpParams().set('mes', mes.toString()).set('anio', anio.toString());
    return this.http.get<MensajeDTO<string>>(`${this.apiUrl}/rotacion/pdf`, { params });
  }

  obtenerBase64ReporteLogistica(): Observable<MensajeDTO<string>> {
    return this.http.get<MensajeDTO<string>>(`${this.apiUrl}/logistica/pdf`);
  }
}
