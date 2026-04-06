import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsuarioConsultaService {
  private apiUrl = `${environment.apiUrl}/api/usuarios`;

  constructor(private http: HttpClient) {}

  consultarPorId(id: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/${id}`);
  }

  consultarPorEmail(email: string): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/email/${email}`);
  }

  filtrarPorSucursal(sucursalId: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/sucursal/${sucursalId}`);
  }

  filtrarPorRol(rol: string): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/rol/${rol}`);
  }

  buscarPorNombre(query?: string, activo?: boolean): Observable<MensajeDTO> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    if (activo !== undefined) params = params.set('activo', activo.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/busqueda`, { params });
  }

  getUsuarios(pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(this.apiUrl, { params });
  }
}
