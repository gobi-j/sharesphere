package com.sharesphere.community.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="community_invitations", uniqueConstraints=@UniqueConstraint(name="uk_invite_token",columnNames="token"))
public class Invitation {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private UUID communityId;
    @Column(nullable=false,length=254) private String email;
    @Column(nullable=false,length=254) private String invitedBy;
    @Column(nullable=false,length=64) private String token;
    @Column(nullable=false) private Instant expiresAt;
    private Instant acceptedAt;
    protected Invitation() {}
    public Invitation(UUID communityId,String email,String invitedBy,String token){this.communityId=communityId;this.email=email.toLowerCase().trim();this.invitedBy=invitedBy.toLowerCase().trim();this.token=token;this.expiresAt=Instant.now().plusSeconds(7*24*3600);}
    public UUID getId(){return id;} public UUID getCommunityId(){return communityId;} public String getEmail(){return email;} public String getInvitedBy(){return invitedBy;} public String getToken(){return token;} public Instant getExpiresAt(){return expiresAt;} public Instant getAcceptedAt(){return acceptedAt;} public void accept(){acceptedAt=Instant.now();}
}
