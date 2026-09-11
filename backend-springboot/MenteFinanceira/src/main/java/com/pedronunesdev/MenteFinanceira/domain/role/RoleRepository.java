package com.pedronunesdev.MenteFinanceira.domain.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByNome(EnumRole nome);
}
