package com.adamo.vrspfab.payments;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refunds")
public class RefundController {

    @GetMapping
    public String getRefunds() {
        return "List of refunds";
    }

    @PostMapping
    public String createRefund() {
        return "Refund created";
    }
}
