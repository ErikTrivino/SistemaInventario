import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionTransferenciasSolicitadasComponent } from './gestion-transferencias-solicitadas.component';

describe('GestionTransferenciasSolicitadasComponent', () => {
  let component: GestionTransferenciasSolicitadasComponent;
  let fixture: ComponentFixture<GestionTransferenciasSolicitadasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionTransferenciasSolicitadasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionTransferenciasSolicitadasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
