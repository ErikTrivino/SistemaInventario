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

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'gestion-proveedor', component: GestionProveedorComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "USUARIO"] } },
      { path: 'editar-proveedor/:id', component: EditarProveedorComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "USUARIO"] } },
      { path: 'crear-proveedor', component: CrearProveedorComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "USUARIO"] } },
      { path: 'gestion-usuario', component: GestionUsuarioComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'editar-usuario/:id', component: EditarUsuarioComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'crear-usuario', component: CrearUsuarioComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN"] } },
      { path: 'gestion-producto', component: GestionProductoComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "USUARIO"] } },
      { path: 'editar-producto/:id', component: EditarProductoComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "USUARIO"] } },
      { path: 'crear-producto', component: CrearProductoComponent, canActivate: [RolesGuard], data: { expectedRole: ["ADMIN", "USUARIO"] } },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: 'login', component: LoginComponent, canActivate: [LoginGuard] },
  { path: "**", pathMatch: "full", redirectTo: "dashboard" }
];
