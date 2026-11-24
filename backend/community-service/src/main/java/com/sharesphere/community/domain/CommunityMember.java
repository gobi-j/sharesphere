package com.sharesphere.community.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="community_members", uniqueConstraints=@UniqueConstraint(name="uk_community_member",columnNames={"communityId","email"}))
public class CommunityMember {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID communityId;
 @Column(nullable=false,length=254) private String email;
 @Column(nullable=false,length=20) private String role="MEMBER";
 @Column(nullable=false) private Instant joinedAt=Instant.now();
 protected CommunityMember(){}
 public CommunityMember(UUID communityId,String email,String role){this.communityId=communityId;this.email=email.toLowerCase().trim();this.role=role;}
 public UUID getId(){return id;} public UUID getCommunityId(){return communityId;} public String getEmail(){return email;} public String getRole(){return role;} public Instant getJoinedAt(){return joinedAt;}
}
