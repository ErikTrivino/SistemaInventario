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
