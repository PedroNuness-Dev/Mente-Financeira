package com.pedronunesdev.MenteFinanceira.domain.role;

import com.pedronunesdev.MenteFinanceira.enums.role.EnumRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumRole nome;
}
