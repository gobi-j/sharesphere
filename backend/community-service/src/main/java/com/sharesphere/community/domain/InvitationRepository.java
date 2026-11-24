package com.sharesphere.community.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InvitationRepository extends JpaRepository<Invitation,UUID> { Optional<Invitation> findByToken(String token); List<Invitation> findByCommunityId(UUID id); }
