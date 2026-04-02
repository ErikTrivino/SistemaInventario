import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensajeDTO } from '../modelo/mensaje-dto';
import { environment } from '../../environments/environment';
import { CrearProducto } from '../modelo/crearObjetos';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private apiUrl = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) { }

  // Productos
  createProduct(dto: CrearProducto): Observable<MensajeDTO> {
    return this.http.post<MensajeDTO>(`${this.apiUrl}/productos`, dto);
  }

  getProducts(pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/productos`, { params });
  }

  consultarPorId(id: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/productos/${id}`);
  }

  getProductByIdSucursal(idSucursal: number, idProducto: number): Observable<MensajeDTO> {
    return this.http.get<MensajeDTO>(`${this.apiUrl}/productos/${idSucursal}/${idProducto}`);
  }

  updateProduct(id: number, dto: any): Observable<MensajeDTO> {
    return this.http.put<MensajeDTO>(`${this.apiUrl}/productos/${id}`, dto);
  }

  deleteProduct(id: number): Observable<MensajeDTO> {
    return this.http.delete<MensajeDTO>(`${this.apiUrl}/productos/${id}`);
  }

  // Inventario por sucursal
  getInventoryByBranch(branchId: number, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/inventario/${branchId}`, { params });
  }

  getCatalogo(branchId: number, pagina?: number, porPagina: number = 10): Observable<MensajeDTO> {
    let params = new HttpParams().set('porPagina', porPagina.toString());
    if (pagina !== undefined) params = params.set('pagina', pagina.toString());
    return this.http.get<MensajeDTO>(`${this.apiUrl}/catalogo/${branchId}`, { params });
  }

  updateStock(productId: number, branchId: number, quantity: number, type: string, reason: string, usuarioResponsable: string = 'sistema'): Observable<MensajeDTO> {
    const params = new HttpParams()
      .set('productId', productId.toString())
      .set('branchId', branchId.toString())
      .set('quantity', quantity.toString())
      .set('type', type)
      .set('reason', reason)
      .set('usuarioResponsable', usuarioResponsable);
    return this.http.put<MensajeDTO>(`${this.apiUrl}/inventario/actualizar-stock`, null, { params });
  }
}
