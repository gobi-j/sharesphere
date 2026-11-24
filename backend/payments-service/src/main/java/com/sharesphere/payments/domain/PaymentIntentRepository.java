package com.sharesphere.payments.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent,UUID>{Optional<PaymentIntent> findByBookingId(UUID bookingId);}
