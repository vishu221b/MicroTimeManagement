package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findByOwnerAndIsActiveTrue(String owner);
}
