package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.SubscriptionDTO;

public interface BillingService {

    /** The current user's subscription (creates a FREE one on first access). */
    SubscriptionDTO getForCurrentUser();

    /** Start an upgrade. Stubbed: simulates PRO unless a real provider is wired. */
    SubscriptionDTO checkout();

    /** Cancel back to the FREE tier. */
    SubscriptionDTO cancel();
}
