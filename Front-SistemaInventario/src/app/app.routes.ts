import { Routes } from '@angular/router';
import { GestionProveedorComponent } from './componentes/gestion-proveedor/gestion-proveedor.component';
import { EditarProveedorComponent } from './componentes/editar-proveedor/editar-proveedor.component';
import { CrearProveedorComponent } from './componentes/crear-proveedor/crear-proveedor.component';
import { GestionUsuarioComponent } from './componentes/gestion-usuario/gestion-usuario.component';
import { EditarUsuarioComponent } from './componentes/editar-usuario/editar-usuario.component';
import { CrearUsuarioComponent } from './componentes/crear-usuario/crear-usuario.component';
import { GestionProductoComponent } from './componentes/gestion-producto/gestion-producto.component';
import { EditarProductoComponent } from './componentes/editar-producto/editar-producto.component';
import { CrearProductoComponent } from './componentes/crear-producto/crear-producto.component';
import { RolesGuard } from './guards/roles.service';
import { LoginGuard } from './guards/permiso.service';
import { LoginComponent } from './componentes/login/login.component';
import { DashboardComponent } from './componentes/dashboard/dashboard.component';
import { LayoutComponent } from './componentes/layout/layout.component';

// New Components
import { GestionMovimientosComponent } from './componentes/gestion-movimientos/gestion-movimientos.component';
import { GestionTransferenciasComponent } from './componentes/gestion-transferencias/gestion-transferencias.component';
import { GestionTransferenciasSolicitadasComponent } from './componentes/gestion-transferencias-solicitadas/gestion-transferencias-solicitadas.component';
import { GestionOrdenesCompraComponent } from './componentes/gestion-ordenes-compra/gestion-ordenes-compra.component';
import { GestionRutasComponent } from './componentes/gestion-rutas/gestion-rutas.component';
import { SeguimientoEnviosComponent } from './componentes/seguimiento-envios/seguimiento-envios.component';
import { GestionSucursalesComponent } from './componentes/gestion-sucursales/gestion-sucursales.component';
import { GestionVentasComponent } from './componentes/gestion-ventas/gestion-ventas.component';
import { GestionReportesComponent } from './componentes/gestion-reportes/gestion-reportes.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },

      // Inventario
      { path: 'gestion-producto', component: GestionProductoComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'editar-producto/:idSucursal/:id', component: EditarProductoComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'crear-producto', component: CrearProductoComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'gestion-movimientos', component: GestionMovimientosComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'gestion-transferencias', component: GestionTransferenciasComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'gestion-transferencias-solicitadas', component: GestionTransferenciasSolicitadasComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },

      // Abastecimiento
      { path: 'gestion-proveedor', component: GestionProveedorComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'editar-proveedor/:id', component: EditarProveedorComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'crear-proveedor', component: CrearProveedorComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'gestion-ordenes-compra', component: GestionOrdenesCompraComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },

      // Logística
      { path: 'gestion-rutas', component: GestionRutasComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },
      { path: 'seguimiento-envios', component: SeguimientoEnviosComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },

      // Comercialización
      { path: 'gestion-ventas', component: GestionVentasComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER", "OPERATOR"] } },

      // Configuración
      { path: 'gestion-usuario', component: GestionUsuarioComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'editar-usuario/:id', component: EditarUsuarioComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'crear-usuario', component: CrearUsuarioComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'gestion-sucursales', component: GestionSucursalesComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'gestion-reportes', component: GestionReportesComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "MANAGER"] } },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: 'login', component: LoginComponent, canActivate: [LoginGuard] },
  { path: "**", pathMatch: "full", redirectTo: "dashboard" }
];
