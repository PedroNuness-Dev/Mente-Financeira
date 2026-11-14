import { TestBed } from '@angular/core/testing';

import { TEMA_KEY, ThemeService } from './theme-service';

describe('ThemeService', () => {
  afterEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-tema');
  });

  it('usa o tema salvo e aplica no <html>', () => {
    localStorage.setItem(TEMA_KEY, 'escuro');

    const service = TestBed.inject(ThemeService);

    expect(service.tema()).toBe('escuro');
    expect(document.documentElement.getAttribute('data-tema')).toBe('escuro');
  });

  it('alternar troca o tema, aplica no <html> e persiste a escolha', () => {
    localStorage.setItem(TEMA_KEY, 'claro');
    const service = TestBed.inject(ThemeService);

    service.alternar();

    expect(service.tema()).toBe('escuro');
    expect(document.documentElement.getAttribute('data-tema')).toBe('escuro');
    expect(localStorage.getItem(TEMA_KEY)).toBe('escuro');
  });
});
