import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';
import { EnvioSeguimientoDTO } from '../modelo/logistica/envio-seguimiento-dto';

@Injectable({
  providedIn: 'root'
})
export class LogisticaService {
  private apiUrl = `${environment.apiUrl}/api/envios`;

  constructor(private http: HttpClient) {}

  getShipments(pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(this.apiUrl, { params });
  }

  createShipment(transferId: number): Observable<MensajeDTO> {
    const params = new HttpParams().set('transferId', transferId.toString());
    return this.http.post<MensajeDTO>(this.apiUrl, null, { params });
  }

  updateShipmentStatus(id: number, status: string): Observable<MensajeDTO> {
    const params = new HttpParams().set('status', status);
    return this.http.put<MensajeDTO>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  getMetrics(): Observable<MensajeDTO<EnvioSeguimientoDTO[]>> {
    return this.http.get<MensajeDTO<EnvioSeguimientoDTO[]>>(`${this.apiUrl}/metrics`);
  }
}
