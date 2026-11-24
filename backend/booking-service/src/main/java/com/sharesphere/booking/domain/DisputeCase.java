package com.sharesphere.booking.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="dispute_cases",indexes=@Index(name="idx_dispute_status",columnList="status,createdAt"))
public class DisputeCase {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID bookingId;
 @Column(nullable=false,length=254) private String openedBy;
 @Column(nullable=false,length=1200) private String reason;
 @Column(nullable=false,length=20) private String status="OPEN";
 @Column(length=254) private String resolvedBy;
 @Column(length=1200) private String resolution;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 private Instant resolvedAt;
 protected DisputeCase(){}
 public DisputeCase(UUID bookingId,String openedBy,String reason){this.bookingId=bookingId;this.openedBy=openedBy.toLowerCase();this.reason=reason.trim();}
 public void resolve(String email,String resolution){this.resolvedBy=email;this.resolution=resolution.trim();this.status="RESOLVED";this.resolvedAt=Instant.now();}
 public UUID getId(){return id;}public UUID getBookingId(){return bookingId;}public String getOpenedBy(){return openedBy;}public String getReason(){return reason;}public String getStatus(){return status;}public String getResolvedBy(){return resolvedBy;}public String getResolution(){return resolution;}public Instant getCreatedAt(){return createdAt;}public Instant getResolvedAt(){return resolvedAt;}
}
