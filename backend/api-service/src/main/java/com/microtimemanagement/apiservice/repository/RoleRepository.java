package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Role findByNameAndIsActiveTrue(String name);

    List<Role> findByNameInAndIsActiveTrue(List<String> name);

    Page<Role> findAll(Pageable pageable);

    Role findByIdAndIsActiveTrue(String id);

    List<Role> findByIdInAndIsActiveTrue(Collection<String> ids);

    Role findByName(String name);

}
