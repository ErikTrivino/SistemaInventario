import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VentaService {
  private apiUrl = `${environment.apiUrl}/api/ventas`;

  constructor(private http: HttpClient) { }

  createSale(dto: any): Observable<MensajeDTO> {
    return this.http.post<MensajeDTO>(this.apiUrl, dto);
  }

  checkStock(productId: number, branchId: number, quantity: number): Observable<MensajeDTO> {
    const params = new HttpParams()
      .set('productId', productId.toString())
      .set('branchId', branchId.toString())
      .set('quantity', quantity.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/validacion-stock`, { params });
  }

  getSalesByBranch(id: number, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/sucursal/${id}`, { params });
  }

  getSalesByDateRange(start: string, end: string, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams()
      .set('start', start)
      .set('end', end)
      .set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/rango-fechas`, { params });
  }

  obtenerComprobanteVenta(id: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/comprobante/${id}`);
  }
}
