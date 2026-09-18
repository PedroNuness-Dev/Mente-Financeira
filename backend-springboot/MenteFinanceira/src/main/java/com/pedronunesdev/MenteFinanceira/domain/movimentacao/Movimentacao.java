package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import com.pedronunesdev.MenteFinanceira.domain.carteira.Carteira;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_movimentacao")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    private Long id;

    @Column(name = "valor_movimentado", nullable = false)
    private BigDecimal valorMovimentado;

    @CreationTimestamp
    @Column(name = "data_de_execucao")
    private LocalDateTime dataDeExecucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "tipo_movimentacao")
    private TipoMovimentacao tipoMovimentacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_movimentacao", nullable = false)
    private CategoriaMovimentacao categoriaMovimentacao;

    @JoinColumn(name = "id_carteira")
    @ManyToOne
    private Carteira carteira;
}
