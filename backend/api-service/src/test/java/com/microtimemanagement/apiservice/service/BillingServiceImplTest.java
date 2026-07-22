package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.SubscriptionDTO;
import com.microtimemanagement.apiservice.enums.PlanTier;
import com.microtimemanagement.apiservice.model.Subscription;
import com.microtimemanagement.apiservice.repository.SubscriptionRepository;
import com.microtimemanagement.apiservice.service.impl.BillingServiceImpl;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Billing Service Tests (stub)")
@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    private static final String UID = "owner-uid-1";

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(currentUserProvider.currentUid()).thenReturn(UID);
        Mockito.lenient().when(subscriptionRepository.save(Mockito.any()))
                .then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    @DisplayName("first access creates a FREE subscription; payments are not configured in the stub")
    void createsFreeOnFirstAccess() {
        Mockito.when(subscriptionRepository.findByOwnerAndIsActiveTrue(UID)).thenReturn(Optional.empty());

        SubscriptionDTO dto = billingService.getForCurrentUser();

        assertThat(dto.getPlan()).isEqualTo(PlanTier.FREE);
        assertThat(dto.getPaymentsConfigured()).isFalse();
    }

    @Test
    @DisplayName("checkout simulates a PRO upgrade while payments are stubbed")
    void checkoutSimulatesPro() {
        Subscription free = Subscription.builder().id("s1").owner(UID).plan(PlanTier.FREE).provider("STUB").build();
        Mockito.when(subscriptionRepository.findByOwnerAndIsActiveTrue(UID)).thenReturn(Optional.of(free));

        SubscriptionDTO dto = billingService.checkout();

        assertThat(dto.getPlan()).isEqualTo(PlanTier.PRO);
        assertThat(dto.getSimulated()).isTrue();
        assertThat(free.getPlan()).isEqualTo(PlanTier.PRO);
    }

    @Test
    @DisplayName("cancel returns the user to the FREE plan")
    void cancelDowngrades() {
        Subscription pro = Subscription.builder().id("s1").owner(UID).plan(PlanTier.PRO).provider("STUB").build();
        Mockito.when(subscriptionRepository.findByOwnerAndIsActiveTrue(UID)).thenReturn(Optional.of(pro));

        SubscriptionDTO dto = billingService.cancel();

        assertThat(dto.getPlan()).isEqualTo(PlanTier.FREE);
        assertThat(pro.getPlan()).isEqualTo(PlanTier.FREE);
    }
}
