package com.sharesphere.notifications.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="notifications",indexes=@Index(name="idx_notification_recipient",columnList="recipientEmail,createdAt"))
public class NotificationRecord {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false,length=254) private String recipientEmail;
 @Column(nullable=false,length=80) private String eventType;
 @Column(nullable=false,length=1000) private String message;
 @Column(length=36) private String referenceId;
 @Column(nullable=false) private boolean read=false;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 protected NotificationRecord(){}
 public NotificationRecord(String recipientEmail,String eventType,String message,String referenceId){this.recipientEmail=recipientEmail.toLowerCase();this.eventType=eventType;this.message=message;this.referenceId=referenceId;}
 public UUID getId(){return id;}public String getRecipientEmail(){return recipientEmail;}public String getEventType(){return eventType;}public String getMessage(){return message;}public String getReferenceId(){return referenceId;}public boolean isRead(){return read;}public Instant getCreatedAt(){return createdAt;}public void markRead(){read=true;}
}
