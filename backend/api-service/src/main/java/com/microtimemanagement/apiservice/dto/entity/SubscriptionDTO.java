package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.PlanTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {

    private PlanTier plan;
    private String provider;

    /** False while payments are stubbed (no real provider configured). */
    private Boolean paymentsConfigured;

    // Populated by the checkout response:
    /** True when the "upgrade" was a stub simulation rather than a real charge. */
    private Boolean simulated;
    /** A provider Checkout URL to redirect to, when payments are configured. */
    private String checkoutUrl;
    /** Human-friendly note for the UI. */
    private String message;
}
