import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Secured } from './secured';

describe('Secured', () => {
  let component: Secured;
  let fixture: ComponentFixture<Secured>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Secured]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Secured);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
