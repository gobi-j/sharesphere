package com.sharesphere.notifications.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<NotificationRecord,UUID>{List<NotificationRecord> findTop100ByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(String email);}
