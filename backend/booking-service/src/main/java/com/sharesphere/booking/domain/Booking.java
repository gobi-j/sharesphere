package com.sharesphere.booking.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="bookings",indexes=@Index(name="idx_booking_people",columnList="requesterEmail,providerEmail"))
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private UUID listingId;
    @Column(nullable=false,length=254) private String requesterEmail;
    @Column(nullable=false,length=254) private String providerEmail;
    @Column(nullable=false,length=16) private String status="REQUESTED";
    @Column(nullable=false,length=16) private String exchangeType;
    @Column(nullable=false,length=254) private String lastOfferBy;
    @Column(precision=10,scale=2) private BigDecimal agreedPriceInr;
    @Column(nullable=false) private LocalDate startDate;
    @Column(nullable=false) private LocalDate endDate;
    @Column(nullable=false,length=600) private String reason;
    @Column(nullable=false) private Instant createdAt=Instant.now();
    @Column(nullable=false) private Instant updatedAt=Instant.now();
    private Instant approvedAt; private Instant handoverAt; private Instant returnedAt;
    protected Booking(){}
    public Booking(UUID listingId,String requesterEmail,String providerEmail,String exchangeType,BigDecimal agreedPriceInr,LocalDate startDate,LocalDate endDate,String reason){this.listingId=listingId;this.requesterEmail=requesterEmail.toLowerCase().trim();this.providerEmail=providerEmail.toLowerCase().trim();this.lastOfferBy=this.requesterEmail;this.exchangeType=exchangeType.toUpperCase();this.agreedPriceInr=agreedPriceInr;this.startDate=startDate;this.endDate=endDate;this.reason=reason.trim();}
    public void negotiate(String type,BigDecimal price,String reason,String actor){this.exchangeType=type.toUpperCase();this.agreedPriceInr=price;this.reason=reason.trim();this.lastOfferBy=actor.toLowerCase();this.status="NEGOTIATING";this.updatedAt=Instant.now();}
    public void acceptOffer(String type,BigDecimal price,String reason,String actor){this.exchangeType=type.toUpperCase();this.agreedPriceInr=price;this.reason=reason.trim();this.lastOfferBy=actor.toLowerCase();this.status="REQUESTED";this.updatedAt=Instant.now();}
    public void approve(){this.approvedAt=Instant.now();this.status=exchangeType.equals("FREE")?"READY_FOR_HANDOVER":"PENDING_PAYMENT";this.updatedAt=Instant.now();}
    public void paymentConfirmed(){if(!status.equals("PENDING_PAYMENT"))throw new IllegalStateException("Booking is not awaiting payment");this.status="READY_FOR_HANDOVER";this.updatedAt=Instant.now();}
    public void decline(){this.status="DECLINED";this.updatedAt=Instant.now();}
    public void cancel(){this.status="CANCELLED";this.updatedAt=Instant.now();}
    public void handover(){this.handoverAt=Instant.now();this.status="ACTIVE";this.updatedAt=Instant.now();}
    public void requestReturn(){this.status="RETURN_PENDING";this.updatedAt=Instant.now();}
    public void confirmReturn(){this.returnedAt=Instant.now();this.status="COMPLETED";this.updatedAt=Instant.now();}
    public void dispute(){this.status="DISPUTED";this.updatedAt=Instant.now();}
    public UUID getId(){return id;} public UUID getListingId(){return listingId;} public String getRequesterEmail(){return requesterEmail;} public String getProviderEmail(){return providerEmail;} public String getStatus(){return status;} public String getExchangeType(){return exchangeType;} public String getLastOfferBy(){return lastOfferBy;} public BigDecimal getAgreedPriceInr(){return agreedPriceInr;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;} public String getReason(){return reason;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public Instant getApprovedAt(){return approvedAt;} public Instant getHandoverAt(){return handoverAt;} public Instant getReturnedAt(){return returnedAt;}
}
