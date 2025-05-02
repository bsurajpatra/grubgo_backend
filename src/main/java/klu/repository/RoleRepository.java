package klu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import klu.model.Role;
import klu.model.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);
}
