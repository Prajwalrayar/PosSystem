package com.zosh.controller;

import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import com.zosh.domain.PaymentGateway;
import com.zosh.exception.UserException;
import com.zosh.modal.PaymentOrder;
import com.zosh.modal.User;
import com.zosh.payload.response.PaymentGatewayStatusResponse;
import com.zosh.payload.response.PaymentLinkResponse;
import com.zosh.service.PaymentService;
import com.zosh.service.UserService;
import com.zosh.service.gateway.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final RazorpayService razorpayService;


//    @PostMapping("/create")
//    public ResponseEntity<PaymentLinkResponse> createPaymentLink(
//            @RequestHeader("Authorization") String jwt,
//            @RequestParam Long planId,
//            @RequestParam PaymentGateway paymentMethod) throws UserException, RazorpayException, StripeException {
//
//
//            User user = userService.getUserFromJwtToken(jwt);
//
//
//
//            PaymentLinkResponse paymentLinkResponse =
//                    paymentService.initiatePayment(user, planId, paymentMethod);
//            return ResponseEntity.ok(paymentLinkResponse);
//
//
//    }

    @GetMapping("/gateway-status")
    public ResponseEntity<PaymentGatewayStatusResponse> getGatewayStatus() {
        List<String> missingFields = new ArrayList<>();
        if (!razorpayService.isConfigured()) {
            missingFields.add("RAZORPAY_API_KEY");
            missingFields.add("RAZORPAY_API_SECRET");
        }

        PaymentGatewayStatusResponse response = PaymentGatewayStatusResponse.builder()
                .gateway("RAZORPAY")
                .configured(razorpayService.isConfigured())
                .upiSupported(true)
                .cardSupported(true)
                .missingFields(missingFields)
                .message(razorpayService.isConfigured()
                        ? "Payment gateway is configured."
                        : "Please add valid Razorpay API key and secret key to enable UPI and card payments.")
                .build();

        return ResponseEntity.ok(response);
    }



//    @PatchMapping("/proceed")
//    public ResponseEntity<Boolean> proceedPayment(
//            @RequestParam String paymentId,
//            @RequestParam String paymentLinkId) throws Exception {
//
//            PaymentOrder paymentOrder = paymentService.
//                    getPaymentOrderByPaymentId(paymentLinkId);
//            Boolean success = paymentService.ProceedPaymentOrder(
//                    paymentOrder,
//                    paymentId, paymentLinkId);
//            return ResponseEntity.ok(success);
//
//    }


}
