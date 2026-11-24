package com.sharesphere.chat.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ChatMessageRepository extends JpaRepository<ChatMessage,UUID>{List<ChatMessage> findTop100ByBookingIdOrderBySentAtAsc(UUID bookingId);}
