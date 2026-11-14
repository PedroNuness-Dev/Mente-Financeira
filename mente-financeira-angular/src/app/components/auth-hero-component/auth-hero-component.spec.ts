import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthHeroComponent } from './auth-hero-component';

describe('AuthHeroComponent', () => {
  let fixture: ComponentFixture<AuthHeroComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthHeroComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AuthHeroComponent);
    await fixture.whenStable();
  });

  it('exibe a manchete e o mascote com texto alternativo', () => {
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('h1')?.textContent).toContain('uma vida financeira');
    expect(el.querySelector('img')?.getAttribute('alt')).toContain('Mascote');
  });
});
