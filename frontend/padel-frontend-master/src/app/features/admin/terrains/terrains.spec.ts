import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Terrains } from './terrains';

describe('Terrains', () => {
  let component: Terrains;
  let fixture: ComponentFixture<Terrains>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Terrains]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Terrains);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
