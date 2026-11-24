package com.sharesphere.chat.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="chat_messages",indexes=@Index(name="idx_chat_booking_time",columnList="bookingId,sentAt"))
public class ChatMessage {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID bookingId;
 @Column(nullable=false,length=254) private String senderEmail;
 @Column(nullable=false,length=2000) private String content;
 @Column(nullable=false) private Instant sentAt=Instant.now();
 protected ChatMessage(){}
 public ChatMessage(UUID bookingId,String senderEmail,String content){this.bookingId=bookingId;this.senderEmail=senderEmail.toLowerCase();this.content=content.trim();}
 public UUID getId(){return id;}public UUID getBookingId(){return bookingId;}public String getSenderEmail(){return senderEmail;}public String getContent(){return content;}public Instant getSentAt(){return sentAt;}
}
