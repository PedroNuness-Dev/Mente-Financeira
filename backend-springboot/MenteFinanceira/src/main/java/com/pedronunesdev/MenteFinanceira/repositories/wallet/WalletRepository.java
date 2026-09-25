package com.pedronunesdev.MenteFinanceira.repositories.wallet;

import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet,Long> {

    @Query("""
    SELECT w FROM Wallet w
       WHERE w.user.id = :userId
""")
    Optional<Wallet> findWalletByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT w.balance FROM Wallet w
       WHERE w.user.id = :userId
""")
    BigDecimal checkBalance(@Param("userId") Long userId);
}