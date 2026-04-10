package com.zosh.controller;

import com.zosh.domain.PaymentGateway;
import com.zosh.domain.PaymentStatus;
import com.zosh.domain.SubscriptionStatus;
import com.zosh.exception.PaymentException;
import com.zosh.modal.Subscription;
import com.zosh.payload.response.PaymentInitiateResponse;
import com.zosh.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;


    // 🆕 Store subscribes to a plan (TRIAL or NEW)
    @PostMapping("/subscribe")
    public ResponseEntity<?> createSubscription(
            @RequestParam("storeId") Long storeId,
            @RequestParam("planId") Long planId,
            @RequestParam(defaultValue = "RAZORPAY") PaymentGateway gateway,
            @RequestParam(required = false) String transactionId
    ) throws PaymentException {


        PaymentInitiateResponse res=subscriptionService.createSubscription(storeId, planId, gateway, transactionId);
        return ResponseEntity.ok(res);
    }

    // 🔁 Store upgrades to a new plan (ACTIVE)
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradePlan(
            @RequestParam("storeId") Long storeId,
            @RequestParam("planId") Long planId,
            @RequestParam(defaultValue = "RAZORPAY") PaymentGateway gateway,
            @RequestParam(required = false) String transactionId
    ) throws PaymentException {

        PaymentInitiateResponse res= subscriptionService.upgradeSubscription(storeId, planId, gateway, transactionId);
        return ResponseEntity.ok(res);
    }

    // ✅ Admin activates a subscription
    @PutMapping("/{subscriptionId}/activate")
    public Subscription activateSubscription(@PathVariable("subscriptionId") Long subscriptionId) {
        return subscriptionService.activateSubscription(subscriptionId);
    }

    // ❌ Admin cancels a subscription
    @PutMapping("/{subscriptionId}/cancel")
    public Subscription cancelSubscription(@PathVariable("subscriptionId") Long subscriptionId) {
        return subscriptionService.cancelSubscription(subscriptionId);
    }

    // 💳 Update payment status manually (if needed)
    @PutMapping("/{subscriptionId}/payment-status")
    public Subscription updatePaymentStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam("status") PaymentStatus status
    ) {
        return subscriptionService.updatePaymentStatus(subscriptionId, status);
    }

    // 📦 Store: Get all subscriptions (or by status)
    @GetMapping("/store/{storeId}")
    public List<Subscription> getStoreSubscriptions(
            @PathVariable("storeId") Long storeId,
            @RequestParam(name = "status", required = false) SubscriptionStatus status
    ) {
        return subscriptionService.getSubscriptionsByStore(storeId, status);
    }

    // 🗂️ Admin: Get all subscriptions (optionally filter by status)
    @GetMapping("/admin")
    public List<Subscription> getAllSubscriptions(
            @RequestParam(name = "status", required = false) SubscriptionStatus status
    ) {
        return subscriptionService.getAllSubscriptions(status);
    }

    // ⌛ Admin: Get subscriptions expiring within X days
    @GetMapping("/admin/expiring")
    public List<Subscription> getExpiringSubscriptions(
            @RequestParam(name = "days", defaultValue = "7") int days
    ) {
        return subscriptionService.getExpiringSubscriptionsWithin(days);
    }

    // 📊 Count total subscriptions by status
    @GetMapping("/admin/count")
    public Long countByStatus(
            @RequestParam("status") SubscriptionStatus status
    ) {
        return subscriptionService.countByStatus(status);
    }
}
