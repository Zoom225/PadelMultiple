import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesMatches } from './mes-matches';

describe('MesMatches', () => {
  let component: MesMatches;
  let fixture: ComponentFixture<MesMatches>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesMatches]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MesMatches);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
