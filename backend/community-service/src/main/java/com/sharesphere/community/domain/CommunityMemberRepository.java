package com.sharesphere.community.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommunityMemberRepository extends JpaRepository<CommunityMember,UUID> { List<CommunityMember> findByEmailIgnoreCase(String email); boolean existsByCommunityIdAndEmailIgnoreCase(UUID communityId,String email); boolean existsByCommunityIdAndEmailIgnoreCaseAndRole(UUID communityId,String email,String role); }
