package com.pedronunesdev.MenteFinanceira.repositories.user;

import com.pedronunesdev.MenteFinanceira.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
}