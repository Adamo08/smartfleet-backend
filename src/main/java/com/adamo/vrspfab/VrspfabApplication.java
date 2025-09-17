package com.adamo.vrspfab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class VrspfabApplication {

    public static void main(String[] args) {
        System.out.println("=== SmartFleet Backend Starting ===");
        System.out.println("Startup time: " + LocalDateTime.now());
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        
        SpringApplication.run(VrspfabApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("=== SmartFleet Backend Started Successfully ===");
        System.out.println("Ready time: " + LocalDateTime.now());
        System.out.println("Application is ready to serve requests!");
    }

}
