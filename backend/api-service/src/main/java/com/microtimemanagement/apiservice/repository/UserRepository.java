package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndIsActiveTrue(String username);

    User findByIdAndIsActiveTrue(String id);

    Optional<User> findByEmailAndIsActiveTrue(String username);

    Optional<User> findByEmailOrUsernameAndIsActiveTrue(String email, String username);

    User findByUidAndIsActiveTrue(String id);

    Optional<User> findByUid(String id);

}
