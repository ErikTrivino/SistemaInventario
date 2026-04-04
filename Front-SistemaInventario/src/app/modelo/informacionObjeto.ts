export interface CompraHistoricoRespuestaDTO {
  idOrdenCompra: number;
  idDetalle: number;
  idProducto: number;
  nombreProducto: string;
  idProveedor: number;
  nombreProveedor: string;
  cantidadSolicitada: number;
  cantidadRecibida: number;
  precioUnitario: number;
  fechaCompra: Date;
  estado: string;
}





export interface InformacionUsuario {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  activo: boolean;
  rol: string;
  sucursalAsignadaId?: number;
  motivoInactivacion?: string;
}



export interface InformacionProducto {
  id: number;
  nombre: string;
  descripcion: string;
  sku: string;
  unidadMedidaBase: string;
  precioCostoPromedio: number;
  activo: boolean;
  stockTotal?: number;
  stockActual?: number;
  idProveedor?: number;
  nombreProveedor?: string;
}

export interface InformacionProveedor {
  id: number;
  razonSocial: string;
  nitRut: string;
  contacto: string;
  email: string;
  activo: boolean;
}

export interface InformacionPedido {
  idPedido: number;
  nombrePedido: string;
  fechaPedido: Date;
  estado: string;
  idProveedor: number;
  idEmpleado: number;
}





export interface InformacionDetalleServicioProducto {
  idDetalleServicioProducto: number;
  cantidadUsada: number;
  idServicio: number;
  idProducto: number;
}

export interface InformacionDetallePedido {
  idDetallePedido: number;
  cantidad: number;
  precioUnitario: number;
  idPedido: number;
  idProducto: number;
}

export interface EnvioInfoDTO {
  idEnvio?: number;
  fechaDespacho?: Date;
  tiempoEstimado?: number;
  fechaRecepcionReal?: Date;
  estadoLogistico?: string;
}

export interface ResumenDetalleDTO {
  idProducto: number;
  cantidadSolicitada: number;
  cantidadConfirmada?: number;
  cantidadRecibida?: number;
  motivoDiferencia?: string;
}

export interface InformacionTransferencia {
  idTransferencia: number;
  idSucursalOrigen: number;
  idSucursalDestino: number;
  estado: string;
  fechaSolicitud: Date;
  items: ResumenDetalleDTO[];
  envio?: EnvioInfoDTO;
}

export interface ProductoDetallePorSucursalDTO {
  id: number;
  nombre: string;
  descripcion: string;
  sku: string;
  unidadMedidaBase: string;
  precioCostoPromedio: number;
  stock: number;
  idSucursal: number;
  proveedor: number;
}

export interface VentaInformacionDTO {
  idVenta: number;
  idSucursal: number;
  idUsuarioVendedor: number;
  nombreVendedor?: string;
  fechaVenta: string; // o Date (te explico abajo)
  total: number;
  comprobanteOriginal: string;
}
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // página actual (0-based)
}
export interface ReporteVentasDTO {
  fechaInicio: Date;
  fechaFin: Date;
  totalVentas: number;
  ingresoTotal: number;
  promedioVenta: number;
  porSucursal: Page<ResumenVentaSucursalDTO>;
}

export interface ResumenVentaSucursalDTO {
  idSucursal: number;
  cantidadVentas: number;
  ingresoTotal: number;
}
export interface ReporteInventarioDTO {
  fechaGeneracion: Date;
  idSucursal: number;
  totalProductos: number;
  productosEnStockMinimo: number;
  productosAgotados: number;
  valorTotalInventario: number;
  detalle: Page<ItemInventarioDTO>;
}

export interface ItemInventarioDTO {
  idProducto: number;
  nombreProducto: string;
  sku: string;
  stockActual: number;
  stockMinimo: number;
  estadoStock: 'NORMAL' | 'BAJO' | 'AGOTADO';
}
export interface ReporteTransferenciasDTO {
  fechaInicio: Date;
  fechaFin: Date;
  totalTransferencias: number;
  completadas: number;
  conDiscrepancias: number;
  pendientes: number;
  detalle: Page<ItemTransferenciaDTO>;
}

export interface ItemTransferenciaDTO {
  idTransferencia: number;
  idSucursalOrigen: number;
  idSucursalDestino: number;
  idProducto: number;
  cantidadSolicitada: number;
  cantidadRecibida: number;
  estado: string;
  fechaSolicitud: Date;
  fechaRecepcion: Date;
}
export interface ReporteComparativoDTO {
  anio: number;
  meses: ResumenMensualDTO[];
}

export interface ResumenMensualDTO {
  mes: number;
  nombreMes: string;
  cantidadVentas: number;
  ingresoTotal: number;
  variacionPorcentual: number;
}
export interface ReporteRotacionDTO {
  anio: number;
  mes: number;
  productos: Page<ItemRotacionDTO>;
}

export interface ItemRotacionDTO {
  idProducto: number;
  nombreProducto: string;
  totalSalidas: number;
  valorTotalSalidas: number;
  porcentajeParticipacion: number;
  clasificacion: 'A' | 'B' | 'C';
}

export interface AlertaStockDTO {
  idProducto: number;
  nombreProducto: string;
  idSucursal: number;
  stockActual: number | null;
  stockMinimo: number | null;
  diferencia: number | null;
}