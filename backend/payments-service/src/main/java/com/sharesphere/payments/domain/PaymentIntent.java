package com.sharesphere.payments.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="payment_intents",uniqueConstraints=@UniqueConstraint(name="uk_payment_booking",columnNames="bookingId"))
public class PaymentIntent {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID bookingId;
 @Column(nullable=false,length=254) private String payerEmail;
 @Column(nullable=false,length=254) private String payeeEmail;
 @Column(nullable=false,length=100) private String payeeVpa;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal amountInr;
 @Column(nullable=false,length=20) private String status="PENDING";
 @Column(nullable=false) private Instant createdAt=Instant.now();
 private Instant paidAt;
 @Column(length=120) private String transactionRef;
 protected PaymentIntent(){}
 public PaymentIntent(UUID bookingId,String payerEmail,String payeeEmail,String payeeVpa,BigDecimal amountInr){this.bookingId=bookingId;this.payerEmail=payerEmail.toLowerCase();this.payeeEmail=payeeEmail.toLowerCase();this.payeeVpa=payeeVpa.trim();this.amountInr=amountInr;}
 public void markPaid(String transactionRef){this.status="PAID";this.transactionRef=transactionRef;this.paidAt=Instant.now();}
 public UUID getId(){return id;}public UUID getBookingId(){return bookingId;}public String getPayerEmail(){return payerEmail;}public String getPayeeEmail(){return payeeEmail;}public String getPayeeVpa(){return payeeVpa;}public BigDecimal getAmountInr(){return amountInr;}public String getStatus(){return status;}public Instant getCreatedAt(){return createdAt;}public Instant getPaidAt(){return paidAt;}public String getTransactionRef(){return transactionRef;}
}
