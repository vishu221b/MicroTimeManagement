package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    Role findByNameAndIsActiveTrue(String name);

    List<Role> findByNameInAndIsActiveTrue(List<String> name);

    Page<Role> findAll(Pageable pageable);

    Role findByIdAndIsActiveTrue(String id);

    Optional<Role> findByIdOrNameAndIsActiveTrue(String id, String name);

    Role findByName(String name);

}
