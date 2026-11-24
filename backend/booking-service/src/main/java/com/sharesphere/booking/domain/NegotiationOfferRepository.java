package com.sharesphere.booking.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NegotiationOfferRepository extends JpaRepository<NegotiationOffer,UUID> { List<NegotiationOffer> findByBookingIdOrderByCreatedAtAsc(UUID bookingId); }
