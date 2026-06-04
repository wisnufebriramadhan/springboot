package com.startechinnovation.userapi.service;

import com.startechinnovation.userapi.dto.TransactionNotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Async
    @EventListener
    public void handleTransactionEvent(TransactionNotificationEvent event) {
        // Simulate sending SMS/Email/Push Notification
        System.out.println("--- ASYNC NOTIFICATION START ---");
        System.out.println("Sending notification for transaction: " + event.getReferenceNumber());
        System.out.println("Amount: " + event.getAmount());
        System.out.println("To: " + event.getDestinationAccount());
        
        try {
            Thread.sleep(2000); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Notification sent successfully!");
        System.out.println("--- ASYNC NOTIFICATION END ---");
    }
}
