package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.PlanTier;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A user's subscription. One active row per user (their uid). Billing is
 * stubbed by default ({@code provider = "STUB"}); a real integration would set
 * provider "stripe" and store the Stripe subscription id in {@code externalId}.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_subscription")
@EqualsAndHashCode(callSuper = true)
public class Subscription extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Owning user's uid. */
    private String owner;

    @Enumerated(EnumType.STRING)
    private PlanTier plan;

    /** "STUB" until a real payment provider is wired. */
    private String provider;

    /** Provider-side id (e.g. Stripe subscription id), when applicable. */
    private String externalId;
}
