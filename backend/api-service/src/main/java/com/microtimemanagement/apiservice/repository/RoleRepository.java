package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    Role findByNameAndIsActiveTrue(String name);

    List<Role> findAllByNameAndIsActiveTrue(String name);

    Role findByIdAndIsActiveTrue(String id);

    Optional<Role> findByIdOrNameAndIsActiveTrue(String id, String name);

    Role findByName(String name);

}
