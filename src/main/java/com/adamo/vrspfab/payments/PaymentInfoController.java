package com.adamo.vrspfab.payments;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-info")
public class PaymentInfoController {

    @GetMapping
    public String getPaymentInfo() {
        return "Payment information";
    }

    @PostMapping
    public String createPaymentInfo() {
        return "Payment information created";
    }
}
