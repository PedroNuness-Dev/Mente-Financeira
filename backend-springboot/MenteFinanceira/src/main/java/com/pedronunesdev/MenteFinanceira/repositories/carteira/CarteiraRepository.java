package com.pedronunesdev.MenteFinanceira.repositories.carteira;

import com.pedronunesdev.MenteFinanceira.domain.carteira.Carteira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface CarteiraRepository extends JpaRepository<Carteira,Long> {

    @Query("""
    SELECT c FROM Carteira c
       WHERE c.usuario.id = :id_usuario
""")
    Optional<Carteira> buscarCarteiraPeloIdDoUsuario(@Param("id_usuario") Long idUsuario);

    @Query("""
    SELECT c.saldo FROM Carteira c
       WHERE c.usuario.id = :id_usuario
""")
    BigDecimal consultarSaldo(@Param("id_usuario") Long idUsuario);
}
