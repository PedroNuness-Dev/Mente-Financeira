package com.pedronunesdev.MenteFinanceira.repositories.role;

import com.pedronunesdev.MenteFinanceira.enums.role.RoleType;
import com.pedronunesdev.MenteFinanceira.domain.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(RoleType name);
}