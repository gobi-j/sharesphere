package com.sharesphere.reviews.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewRepository extends JpaRepository<Review,UUID>{List<Review> findBySubjectEmailIgnoreCaseOrderByCreatedAtDesc(String email);boolean existsByBookingIdAndAuthorEmailIgnoreCase(UUID booking,String author);}
