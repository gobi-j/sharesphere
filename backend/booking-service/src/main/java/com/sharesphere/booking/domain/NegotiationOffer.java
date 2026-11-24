package com.sharesphere.booking.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="booking_offers")
public class NegotiationOffer {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID bookingId;
 @Column(nullable=false,length=254) private String senderEmail;
 @Column(nullable=false,length=16) private String exchangeType;
 @Column(precision=10,scale=2) private BigDecimal amountInr;
 @Column(nullable=false,length=600) private String reason;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 private Instant acceptedAt;
 protected NegotiationOffer(){}
 public NegotiationOffer(UUID bookingId,String senderEmail,String exchangeType,BigDecimal amountInr,String reason){this.bookingId=bookingId;this.senderEmail=senderEmail.toLowerCase();this.exchangeType=exchangeType;this.amountInr=amountInr;this.reason=reason.trim();}
 public UUID getId(){return id;}public UUID getBookingId(){return bookingId;}public String getSenderEmail(){return senderEmail;}public String getExchangeType(){return exchangeType;}public BigDecimal getAmountInr(){return amountInr;}public String getReason(){return reason;}public Instant getCreatedAt(){return createdAt;}
 public void markAccepted(){this.acceptedAt=Instant.now();}public Instant getAcceptedAt(){return acceptedAt;}
}
