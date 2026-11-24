package com.sharesphere.booking.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookingRepository extends JpaRepository<Booking,UUID> { List<Booking> findByRequesterEmailIgnoreCaseOrProviderEmailIgnoreCaseOrderByCreatedAtDesc(String requester,String provider); List<Booking> findByListingIdAndStatusIn(UUID listingId,Collection<String> statuses); }
