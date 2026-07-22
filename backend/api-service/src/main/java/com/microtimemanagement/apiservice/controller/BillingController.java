package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.SubscriptionDTO;
import com.microtimemanagement.apiservice.service.BillingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.BillingEndpoint.API_BASE)
@Tag(name = "Billing", description = "Subscription / plan (stubbed)")
public class BillingController {

    private final BillingService billingService;

    @GetMapping
    public SubscriptionDTO current() {
        return billingService.getForCurrentUser();
    }

    @PostMapping("/checkout")
    public SubscriptionDTO checkout() {
        return billingService.checkout();
    }

    @PostMapping("/cancel")
    public SubscriptionDTO cancel() {
        return billingService.cancel();
    }
}
