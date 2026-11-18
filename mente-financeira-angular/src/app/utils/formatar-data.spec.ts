import { formatarDataBr, formatarDataCurta, formatarDataIso, lerDataHora } from './formatar-data';

describe('lerDataHora', () => {
  it('lê o ISO do Jackson como horário local', () => {
    expect(lerDataHora('2026-11-14T13:20:05')).toEqual(new Date(2026, 10, 14, 13, 20, 5));
  });

  it('ignora a fração de segundo com 6 casas (microssegundos do Postgres)', () => {
    expect(lerDataHora('2026-11-14T13:20:05.123456')).toEqual(new Date(2026, 10, 14, 13, 20, 5));
  });

  it('lê uma data ISO sem horário como meia-noite', () => {
    expect(lerDataHora('2026-11-14')).toEqual(new Date(2026, 10, 14));
  });

  it('continua lendo o formato antigo dd/MM/yyyy HH:mm:ss', () => {
    expect(lerDataHora('14/11/2026 13:20:05')).toEqual(new Date(2026, 10, 14, 13, 20, 5));
  });

  it('devolve null para vazio, lixo ou data impossível', () => {
    expect(lerDataHora(undefined)).toBeNull();
    expect(lerDataHora('')).toBeNull();
    expect(lerDataHora('ontem')).toBeNull();
    expect(lerDataHora('2026-02-31T10:00:00')).toBeNull();
  });
});

describe('formatarDataIso', () => {
  it('usa o dia local, com zeros à esquerda', () => {
    expect(formatarDataIso(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('não muda de dia perto da meia-noite (o toISOString mudaria em fusos negativos)', () => {
    expect(formatarDataIso(new Date(2026, 10, 30, 23, 59, 59))).toBe('2026-11-30');
  });
});

describe('formatarDataBr', () => {
  it('escreve o mês por extenso', () => {
    expect(formatarDataBr('2026-11-04T08:00:00')).toBe('04 de novembro de 2026');
  });

  it('devolve vazio quando não consegue ler', () => {
    expect(formatarDataBr(undefined)).toBe('');
  });
});

describe('formatarDataCurta', () => {
  it('mostra dia/mês e hora:minuto', () => {
    expect(formatarDataCurta('2026-11-14T09:05:59.999')).toBe('14/11 às 09:05');
  });
});
