package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.dto.entity.SubscriptionDTO;
import com.microtimemanagement.apiservice.enums.PlanTier;
import com.microtimemanagement.apiservice.model.Subscription;
import com.microtimemanagement.apiservice.repository.SubscriptionRepository;
import com.microtimemanagement.apiservice.service.BillingService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Subscription/billing. STUBBED: with no Stripe key configured, "checkout"
 * simulates a PRO upgrade so the paid-tier UX can be built and tested. When
 * {@code STRIPE_SECRET_KEY} is set, real charging is deliberately not performed
 * here — the README documents wiring Stripe Checkout + webhooks.
 */
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Value("${stripe.secret-key:${STRIPE_SECRET_KEY:}}")
    private String stripeSecretKey;

    private boolean paymentsConfigured() {
        return StringUtils.isNotBlank(stripeSecretKey);
    }

    private Subscription loadOrCreate() {
        String uid = currentUserProvider.currentUid();
        return subscriptionRepository.findByOwnerAndIsActiveTrue(uid)
                .orElseGet(() -> subscriptionRepository.save(Subscription.builder()
                        .owner(uid)
                        .plan(PlanTier.FREE)
                        .provider("STUB")
                        .build()));
    }

    @Override
    public SubscriptionDTO getForCurrentUser() {
        return base(loadOrCreate()).build();
    }

    @Override
    public SubscriptionDTO checkout() {
        Subscription sub = loadOrCreate();
        if (paymentsConfigured()) {
            // A real integration would create a Stripe Checkout Session and
            // return its URL; we don't charge here.
            return base(sub)
                    .simulated(Boolean.FALSE)
                    .message("Stripe is configured but the Checkout integration is not "
                            + "implemented in this build — see README to complete it.")
                    .build();
        }
        // Stub: simulate the upgrade so the paid-tier experience is testable.
        sub.setPlan(PlanTier.PRO);
        sub.setProvider("STUB");
        subscriptionRepository.save(sub);
        return base(sub)
                .simulated(Boolean.TRUE)
                .message("Payments are stubbed — simulated upgrade to PRO. "
                        + "See README to enable real Stripe billing.")
                .build();
    }

    @Override
    public SubscriptionDTO cancel() {
        Subscription sub = loadOrCreate();
        sub.setPlan(PlanTier.FREE);
        subscriptionRepository.save(sub);
        return base(sub).message("Subscription cancelled — back on the Free plan.").build();
    }

    private SubscriptionDTO.SubscriptionDTOBuilder base(Subscription sub) {
        return SubscriptionDTO.builder()
                .plan(sub.getPlan())
                .provider(sub.getProvider())
                .paymentsConfigured(paymentsConfigured());
    }
}
