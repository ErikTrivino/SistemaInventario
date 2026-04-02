export interface CrearCliente {
  nombre: string;
  apellido: string;
  identificacion: number;


}

export interface CrearVehiculo {

  matricula: string;
  modelo: string;
  description: string;
  idCliente: number;

}

export interface CrearServicio {

  fecha: Date;
  descripcion: string;
  idCliente: number;
  id_empleado: number;

}

export interface CrearUsuario {

  nombre: string;
  apellido: string;
  email: string;
  password?: string;
  rol: string;
  sucursalAsignadaId?: number;
  activo?: boolean;
  motivoInactivacion?: string;

}

export interface CrearFactura {

  fechaFactura: Date;
  total: number;
  metodoPago: string;
  idCliente: number;
  idEmpleado: number;


}

export interface CrearProducto {

  nombre: string;
  descripcion: string;
  sku: string;
  unidadMedidaBase: string;
  precioCostoPromedio: number;
  cantidadInicial: number;
  cantidadMinima: number;
  idSucursal: number;
  activo?: boolean;

}

export interface CrearProveedor {

  nitRut: string;
  razonSocial: string;
  contacto: string;
  email: string;

}





export interface CrearDetalleServicioProducto {

  cantidadUsada: number;
  idServicio: number;
  idProducto: number;


}

export interface CrearDetallePedido {

  cantidad: number;
  precioUnitario: number;
  idPedido: number;
  idProducto: number;

}

export interface ItemTransferenciaDTO {
  idProducto: number;
  cantidad: number;
}

export interface TransferenciaCrearDTO {
  idSucursalOrigen: number;
  idSucursalDestino: number;
  items: ItemTransferenciaDTO[];
}

export interface TransferenciaPrepararDTO {
  idTransferencia: number;
  cantidadConfirmada: number;
}

export interface TransferenciaConfirmarEnvioDTO {
  idTransferencia: number;
}

export interface TransferenciaRecepcionDTO {
  idTransferencia: number;
  cantidadRecibida: number;
}

export interface DetalleCompraCrearDTO {
  idProducto: number;
  cantidad: number;
  precioUnitario: number;
  descuentoPorcentaje?: number;
}

export interface OrdenCompraCrearDTO {
  idSucursalDestino: number;
  idProveedor: number;
  plazoPagoDias?: number;
  detalles: DetalleCompraCrearDTO[];
}

export interface DetalleRecepcionDTO {
  idDetalle: number;
  cantidadRecibida: number;
}

export interface OrdenCompraRecepcionDTO {
  idOrdenCompra: number;
  idSucursalDestino: number;
  detallesRecibidos: DetalleRecepcionDTO[];
}
