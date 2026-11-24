package com.sharesphere.community.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="communities")
public class Community {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false, length=100) private String name;
    @Column(nullable=false, length=80) private String city;
    @Column(nullable=false, length=254) private String ownerEmail;
    @Column(nullable=false) private Instant createdAt=Instant.now();
    protected Community() {}
    public Community(String name,String city,String ownerEmail){this.name=name.trim();this.city=city.trim();this.ownerEmail=ownerEmail.toLowerCase().trim();}
    public UUID getId(){return id;} public String getName(){return name;} public String getCity(){return city;} public String getOwnerEmail(){return ownerEmail;} public Instant getCreatedAt(){return createdAt;}
}
