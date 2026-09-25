package com.pedronunesdev.MenteFinanceira.domain.movement;

import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;
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
@Table(name = "tb_movement")
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "moved_amount", nullable = false)
    private BigDecimal movedAmount;

    @CreationTimestamp
    @Column(name = "execution_date")
    private LocalDateTime executionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "movement_type")
    private MovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_category", nullable = false)
    private MovementCategory movementCategory;

    @JoinColumn(name = "wallet_id")
    @ManyToOne
    private Wallet wallet;
}