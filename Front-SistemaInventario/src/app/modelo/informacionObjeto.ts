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
