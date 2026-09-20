package com.pedronunesdev.MenteFinanceira.repositories.movimentacao;

import com.pedronunesdev.MenteFinanceira.dto.movimentacao.CategoriaTotalDTO;
import com.pedronunesdev.MenteFinanceira.domain.movimentacao.Movimentacao;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.MovimentacaoDTOResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao,Long> {

    @Query("""
    SELECT m FROM Movimentacao m
    WHERE m.carteira.usuario.id = :id
    ORDER BY dataDeExecucao DESC
""")
    Page<MovimentacaoDTOResponse> historicoMovimentacoes(@Param("id") Long idUsuario, Pageable pageable);

    @Query("""
    SELECT
        m.categoriaMovimentacao,
        m.tipoMovimentacao,
        SUM(m.valorMovimentado)
    FROM Movimentacao m
    WHERE m.carteira.usuario.id = :id
    AND m.dataDeExecucao BETWEEN :diaPrimeiro AND :diaUltimo
    GROUP BY m.categoriaMovimentacao, m.tipoMovimentacao
    ORDER BY SUM(m.valorMovimentado) DESC
""")
    List<CategoriaTotalDTO> totalPorCategoria(@Param("id") Long idUsuario, @Param("diaPrimeiro") LocalDateTime diaPrimeiro, @Param("diaUltimo") LocalDateTime diaUltimo);
}
