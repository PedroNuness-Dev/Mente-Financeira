package com.pedronunesdev.MenteFinanceira.domain.wallet;

import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.domain.user.User;
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
@Table(name = "tb_wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @NotNull
    @Column(nullable = false)
    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "wallet")
    @Builder.Default
    private List<Movement> movements = new ArrayList<>();

    public void deposit(BigDecimal depositRequest){
        Assert.notNull(depositRequest, "Deposit to the wallet cannot be null");
        this.balance = this.balance.add(depositRequest);
    }

    public void withdraw(BigDecimal withdrawRequest){
        Assert.notNull(withdrawRequest, "Withdrawal from the wallet cannot be null");
        this.balance = this.balance.subtract(withdrawRequest);
    }
}