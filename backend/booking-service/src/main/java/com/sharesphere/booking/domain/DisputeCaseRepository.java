package com.sharesphere.booking.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DisputeCaseRepository extends JpaRepository<DisputeCase,UUID>{List<DisputeCase> findAllByOrderByCreatedAtDesc();}
