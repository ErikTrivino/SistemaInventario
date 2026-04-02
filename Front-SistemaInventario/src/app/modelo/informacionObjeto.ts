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
}

export interface InformacionCliente {
  idCliente: number;
  nombre: string;
  apellido: string;
  identificacion: number;
}

export interface InformacionVehiculo {
  idVehiculo: number;
  matricula: string;
  modelo: string;
  description: string;
  idCliente: number;
}

export interface InformacionServicio {
  idServicio: number;
  fecha: Date;
  descripcion: string;
  idCliente: number;
  idEmpleado: number;
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

export interface InformacionFactura {
  idFactura: number;
  fechaFactura: Date;
  total: number;
  metodoPago: string;
  idCliente: number;
  idEmpleado: number;
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

export interface InformacionDetalleFactura {
  idDetalleFactura: number;
  costServicio: number;
  idServicio: number;
  idFactura: number;
}

export interface InformacionDetalleServicioVehiculo {
  idDetalleServicioVehiculo: number;
  descripcion: string;
  idVehiculo: number;
  idServicio: number;
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
