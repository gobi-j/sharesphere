package com.sharesphere.community.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommunityRepository extends JpaRepository<Community,UUID> { List<Community> findByOwnerEmailIgnoreCase(String email); }
