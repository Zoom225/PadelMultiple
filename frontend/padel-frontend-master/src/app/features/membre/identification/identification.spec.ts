import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Identification } from './identification';

describe('Identification', () => {
  let component: Identification;
  let fixture: ComponentFixture<Identification>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Identification]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Identification);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
