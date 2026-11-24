package com.sharesphere.community.api;

import com.sharesphere.community.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

@RestController @RequestMapping("/api/communities")
public class CommunityController {
    private final CommunityRepository communities; private final CommunityMemberRepository members; private final InvitationRepository invitations;private final String internalToken;
    private static final SecureRandom RANDOM=new SecureRandom();
    public CommunityController(CommunityRepository communities,CommunityMemberRepository members,InvitationRepository invitations,@Value("${sharesphere.internal.token}")String internalToken){this.communities=communities;this.members=members;this.invitations=invitations;this.internalToken=internalToken;}

    @GetMapping public List<CommunityView> mine(@RequestHeader("X-User-Email") String email){
        Map<UUID,CommunityView> result=new LinkedHashMap<>();
        members.findByEmailIgnoreCase(email).forEach(m->communities.findById(m.getCommunityId()).ifPresent(c->result.put(c.getId(),view(c,m.getRole()))));
        communities.findByOwnerEmailIgnoreCase(email).forEach(c->result.putIfAbsent(c.getId(),view(c,"ADMIN")));
        return List.copyOf(result.values());
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public CommunityView create(@Valid @RequestBody CreateCommunity request,@RequestHeader("X-User-Email") String email){
        Community c=communities.save(new Community(request.name(),request.city(),email));members.save(new CommunityMember(c.getId(),email,"ADMIN"));return view(c,"ADMIN");
    }
    @PostMapping("/{id}/invitations") @ResponseStatus(HttpStatus.CREATED) public InvitationView invite(@PathVariable UUID id,@Valid @RequestBody InviteRequest request,@RequestHeader("X-User-Email") String actor){
        Community c=communities.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Community not found"));
        boolean authorized=c.getOwnerEmail().equalsIgnoreCase(actor)||members.existsByCommunityIdAndEmailIgnoreCase(id,actor);
        if(!authorized)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only a community member can invite others");
        byte[] bytes=new byte[24];RANDOM.nextBytes(bytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Invitation invitation=invitations.save(new Invitation(id,request.email(),actor,token));return new InvitationView(invitation.getId(),c.getName(),invitation.getEmail(),invitation.getToken(),invitation.getExpiresAt());
    }
    @PostMapping("/invitations/{token}/accept") public CommunityView accept(@PathVariable String token,@RequestHeader("X-User-Email") String email){
        Invitation invite=invitations.findByToken(token).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation not found"));
        if(invite.getAcceptedAt()!=null||invite.getExpiresAt().isBefore(java.time.Instant.now()))throw new ResponseStatusException(HttpStatus.GONE,"Invitation has expired or has already been used");
        if(!invite.getEmail().equalsIgnoreCase(email))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This invitation belongs to a different email address");
        invite.accept();invitations.save(invite);members.save(new CommunityMember(invite.getCommunityId(),email,"MEMBER"));
        return communities.findById(invite.getCommunityId()).map(c->view(c,"MEMBER")).orElseThrow();
    }
    @GetMapping("/{id}/invitations") public List<InvitationView> listInvites(@PathVariable UUID id,@RequestHeader("X-User-Email") String actor){
        if(!members.existsByCommunityIdAndEmailIgnoreCase(id,actor))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Community membership required");
        return invitations.findByCommunityId(id).stream().map(i->new InvitationView(i.getId(),communities.findById(id).map(Community::getName).orElse("Community"),i.getEmail(),i.getToken(),i.getExpiresAt())).collect(Collectors.toList());
    }
    @GetMapping("/internal/{id}/members/{email}") public boolean isMember(@PathVariable UUID id,@PathVariable String email,@RequestHeader("X-Internal-Token")String token){if(!internalToken.equals(token))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Internal service authentication failed");return communities.findById(id).map(c->c.getOwnerEmail().equalsIgnoreCase(email)||members.existsByCommunityIdAndEmailIgnoreCase(id,email)).orElse(false);}
    private CommunityView view(Community c,String role){return new CommunityView(c.getId(),c.getName(),c.getCity(),role,c.getCreatedAt());}
    public record CreateCommunity(@NotBlank @Size(min=3,max=100) String name,@NotBlank @Size(max=80) String city){}
    public record InviteRequest(@jakarta.validation.constraints.Email @NotBlank String email){}
    public record CommunityView(UUID id,String name,String city,String role,java.time.Instant createdAt){}
    public record InvitationView(UUID id,String communityName,String email,String token,java.time.Instant expiresAt){}
}
