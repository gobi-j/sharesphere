package com.sharesphere.reviews.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="reviews",uniqueConstraints=@UniqueConstraint(name="uk_review_booking_author",columnNames={"booking_id","author_email"}))
public class Review {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="booking_id",nullable=false) private UUID bookingId;
 @Column(name="author_email",nullable=false,length=254) private String authorEmail;
 @Column(nullable=false,length=254) private String subjectEmail;
 @Column(nullable=false) private int rating;
 @Column(nullable=false,length=1200) private String comment;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 protected Review(){}
 public Review(UUID bookingId,String authorEmail,String subjectEmail,int rating,String comment){this.bookingId=bookingId;this.authorEmail=authorEmail.toLowerCase();this.subjectEmail=subjectEmail.toLowerCase();this.rating=rating;this.comment=comment.trim();}
 public UUID getId(){return id;}public UUID getBookingId(){return bookingId;}public String getAuthorEmail(){return authorEmail;}public String getSubjectEmail(){return subjectEmail;}public int getRating(){return rating;}public String getComment(){return comment;}public Instant getCreatedAt(){return createdAt;}
}
