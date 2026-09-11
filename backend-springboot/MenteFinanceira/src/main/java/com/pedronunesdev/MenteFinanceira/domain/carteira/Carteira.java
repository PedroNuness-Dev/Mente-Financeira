package com.pedronunesdev.MenteFinanceira.domain.carteira;

import com.pedronunesdev.MenteFinanceira.domain.movimentacao.Movimentacao;
import com.pedronunesdev.MenteFinanceira.domain.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_carteira")
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carteira")
    private Long id;

    @NotNull
    @Column(nullable = false)
    private BigDecimal saldo;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carteira")
    @Builder.Default
    private List<Movimentacao> movimentacaos = new ArrayList<>();

    public void depositar(BigDecimal deposito){
        Assert.notNull(deposito, "Deposito para a carteira não pode ser null");
        this.saldo = this.saldo.add(deposito);
    }

    public void saquar(BigDecimal saque){
        Assert.notNull(saque, "Saque para a carteira não pode ser null");
        this.saldo = this.saldo.subtract(saque);
    }
}
