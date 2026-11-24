package com.sharesphere.reviews.api;
import com.sharesphere.reviews.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
@RestController @RequestMapping("/api/reviews") public class ReviewController {
 private final ReviewRepository reviews;private final RestClient bookings;private final String internalToken;
 public ReviewController(ReviewRepository reviews,@Value("${services.booking:http://localhost:8084}")String bookingUrl,@Value("${sharesphere.internal.token}")String internalToken){this.reviews=reviews;this.bookings=RestClient.builder().baseUrl(bookingUrl).build();this.internalToken=internalToken;}
 @GetMapping("/members/{email}") public List<ReviewView> forMember(@PathVariable String email){return reviews.findBySubjectEmailIgnoreCaseOrderByCreatedAtDesc(email).stream().map(this::view).toList();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ReviewView create(@Valid @RequestBody ReviewRequest req,@RequestHeader("X-User-Email")String author){if(author.equalsIgnoreCase(req.subjectEmail()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot review yourself");Map<?,?> booking;try{booking=bookings.get().uri("/api/bookings/internal/{id}/payment-details",req.bookingId()).header("X-Internal-Token",internalToken).retrieve().body(Map.class);}catch(Exception ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found",ex);}if(!"COMPLETED".equals(booking.get("status")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Reviews open after exchange completion");String requester=String.valueOf(booking.get("requesterEmail")),provider=String.valueOf(booking.get("providerEmail"));if(!author.equalsIgnoreCase(requester)&&!author.equalsIgnoreCase(provider))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only exchange participants can review");String expected=author.equalsIgnoreCase(requester)?provider:requester;if(!expected.equalsIgnoreCase(req.subjectEmail()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Review the other exchange participant");if(reviews.existsByBookingIdAndAuthorEmailIgnoreCase(req.bookingId(),author))throw new ResponseStatusException(HttpStatus.CONFLICT,"You have already reviewed this exchange");return view(reviews.save(new Review(req.bookingId(),author,req.subjectEmail(),req.rating(),req.comment())));}
 private ReviewView view(Review r){return new ReviewView(r.getId(),r.getBookingId(),r.getAuthorEmail(),r.getSubjectEmail(),r.getRating(),r.getComment(),r.getCreatedAt());}
 public record ReviewRequest(@NotNull UUID bookingId,@Email @NotBlank String subjectEmail,@Min(1) @Max(5) int rating,@NotBlank @Size(max=1200) String comment){}
 public record ReviewView(UUID id,UUID bookingId,String authorEmail,String subjectEmail,int rating,String comment,java.time.Instant createdAt){}
}
