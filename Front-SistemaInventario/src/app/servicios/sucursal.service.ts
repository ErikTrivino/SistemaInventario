import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SucursalService {
  private apiUrl = `${environment.apiUrl}/api/sucursales`;

  constructor(private http: HttpClient) {}

  listar(): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(this.apiUrl);
  }

  consultarPorId(id: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/${id}`);
  }
}
