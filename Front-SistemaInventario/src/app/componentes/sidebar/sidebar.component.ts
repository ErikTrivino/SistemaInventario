import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TokenService } from '../../servicios/token.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  constructor(private tokenService: TokenService) { }

  public logout(): void {
    this.tokenService.logout();
  }

  public isLogged(): boolean {
    return this.tokenService.isLogged();
  }

  public isAdmin(): boolean {
    return this.tokenService.getRol() === 'ADMIN';
  }

  public isGerente(): boolean {
    return this.tokenService.getRol() === 'GERENTE';
  }

  public isOperador(): boolean {
    return this.tokenService.getRol() === 'OPERADOR';
  }
}
