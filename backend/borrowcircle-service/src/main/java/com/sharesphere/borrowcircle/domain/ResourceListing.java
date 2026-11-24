package com.sharesphere.borrowcircle.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="resource_listings", indexes={@Index(name="idx_listing_community",columnList="communityId,status"),@Index(name="idx_listing_category",columnList="category")})
public class ResourceListing {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,length=140) private String title;
    @Column(nullable=false,length=1600) private String description;
    @Column(nullable=false,length=50) private String category;
    @Column(nullable=false) private UUID communityId;
    @Column(nullable=false,length=100) private String communityName;
    @Column(nullable=false,length=254) private String providerEmail;
    @Column(nullable=false,length=12) private String visibility;
    @Column(nullable=false,length=14) private String exchangeType;
    @Column(nullable=false, columnDefinition="boolean default false") private boolean negotiable=false;
    @Column(precision=10,scale=2) private BigDecimal priceInr;
    @Column(nullable=false,length=10) private String priceUnit="DAY";
    @Column(precision=9,scale=6) private BigDecimal latitude;
    @Column(precision=9,scale=6) private BigDecimal longitude;
    @Column(length=600) private String imageUrl;
    private LocalDate availableFrom;
    private LocalDate availableTo;
    @Column(nullable=false,length=16) private String status="ACTIVE";
    @Column(nullable=false) private Instant createdAt=Instant.now();
    @Column(nullable=false) private Instant updatedAt=Instant.now();

    protected ResourceListing(){}
    public ResourceListing(String title,String description,String category,UUID communityId,String communityName,String providerEmail,String visibility,String exchangeType,boolean negotiable,BigDecimal priceInr,String priceUnit,BigDecimal latitude,BigDecimal longitude,String imageUrl,LocalDate availableFrom,LocalDate availableTo){
        this.title=title.trim();this.description=description.trim();this.category=category.trim();this.communityId=communityId;this.communityName=communityName.trim();this.providerEmail=providerEmail.toLowerCase().trim();this.visibility=visibility;this.exchangeType=exchangeType;this.negotiable=negotiable;this.priceInr=priceInr;this.priceUnit=priceUnit;this.latitude=latitude;this.longitude=longitude;this.imageUrl=imageUrl;this.availableFrom=availableFrom;this.availableTo=availableTo;
    }
    public void update(String title,String description,String category,String visibility,String exchangeType,boolean negotiable,BigDecimal priceInr,String priceUnit,BigDecimal latitude,BigDecimal longitude,String imageUrl,LocalDate availableFrom,LocalDate availableTo){this.title=title.trim();this.description=description.trim();this.category=category.trim();this.visibility=visibility;this.exchangeType=exchangeType;this.negotiable=negotiable;this.priceInr=priceInr;this.priceUnit=priceUnit;this.latitude=latitude;this.longitude=longitude;this.imageUrl=imageUrl;this.availableFrom=availableFrom;this.availableTo=availableTo;this.updatedAt=Instant.now();}
    public void archive(){this.status="ARCHIVED";this.updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public String getCategory(){return category;} public UUID getCommunityId(){return communityId;} public String getCommunityName(){return communityName;} public String getProviderEmail(){return providerEmail;} public String getVisibility(){return visibility;} public String getExchangeType(){return exchangeType;} public boolean isNegotiable(){return negotiable||"NEGOTIABLE".equalsIgnoreCase(exchangeType);} public BigDecimal getPriceInr(){return priceInr;} public String getPriceUnit(){return priceUnit;} public BigDecimal getLatitude(){return latitude;} public BigDecimal getLongitude(){return longitude;} public String getImageUrl(){return imageUrl;} public LocalDate getAvailableFrom(){return availableFrom;} public LocalDate getAvailableTo(){return availableTo;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
