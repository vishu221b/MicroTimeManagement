package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    Role findByNameAndIsActiveTrue(String name);

    List<Role> findAllByNameAndIsActiveTrue(String name);

    Role findByIdAndIsActiveTrue(String id);

    Role findByName(String name);

}
