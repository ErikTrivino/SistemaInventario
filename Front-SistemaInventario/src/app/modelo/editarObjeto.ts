


export interface EditarUsuario {
  idUsuario: number;
  nombre: string;
  apellido: string;
  identificacion: number;
  numerophone: number;
  edad: number;
  correo: string;
  password: string;
  estado: string;
  rol: string;
}



export interface EditarProducto {
  nombre: string;
  descripcion: string;
  sku: string;
  unidadMedidaBase: string;
  precioCostoPromedio: number;
  stock: number;
  idSucursal: number;
  idProveedor: number;
  idUsuarioResponsable: number;
  razonCambio: string;
}

export interface EditarProveedor {
  idProveedor: number;
  nombre: string;
  identificacion: number;
  telefono: number;
  correo: string;
}








