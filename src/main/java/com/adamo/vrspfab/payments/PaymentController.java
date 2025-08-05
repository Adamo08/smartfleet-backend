package com.adamo.vrspfab.payments;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @GetMapping
    public String getPayments() {
        return "List of payments";
    }

    @PostMapping
    public String createPayment() {
        return "Payment created";
    }
}
