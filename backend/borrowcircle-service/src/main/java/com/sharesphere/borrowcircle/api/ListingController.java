package com.sharesphere.borrowcircle.api;

import com.sharesphere.borrowcircle.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@RestController @RequestMapping("/api/borrowcircle/listings")
public class ListingController {
    private final ResourceListingRepository listings;private final RestClient communities;private final String internalToken;
    public ListingController(ResourceListingRepository listings,@Value("${services.community:http://localhost:8082}")String communityUrl,@Value("${sharesphere.internal.token}")String internalToken){this.listings=listings;this.communities=RestClient.builder().baseUrl(communityUrl).build();this.internalToken=internalToken;}

    @GetMapping public List<ListingView> discover(@RequestParam UUID communityId,@RequestParam(defaultValue="false") boolean nearby,
            @RequestParam(required=false) String q,@RequestParam(required=false) String category,@RequestParam(required=false) String exchangeType,
            @RequestParam(required=false) BigDecimal latitude,@RequestParam(required=false) BigDecimal longitude,@RequestParam(defaultValue="25") double radiusKm){
        String query=q==null?"":q.trim().toLowerCase();
        return listings.findAll().stream().filter(l->l.getStatus().equals("ACTIVE"))
                .filter(l->nearby?(l.getCommunityId().equals(communityId)||l.getVisibility().equals("PUBLIC")):l.getCommunityId().equals(communityId))
                .filter(l->category==null||category.isBlank()||l.getCategory().equalsIgnoreCase(category))
                .filter(l->exchangeType==null||exchangeType.isBlank()||l.getExchangeType().equalsIgnoreCase(exchangeType))
                .filter(l->query.isBlank()||(l.getTitle()+" "+l.getDescription()+" "+l.getCategory()).toLowerCase().contains(query))
                .map(l->toView(l,latitude,longitude))
                .filter(v->!nearby||latitude==null||longitude==null||v.distanceKm()==null||v.distanceKm()<=radiusKm)
                .sorted(Comparator.comparing(v->v.distanceKm()==null?Double.MAX_VALUE:v.distanceKm()))
                .collect(Collectors.toList());
    }

    @GetMapping("/mine") public List<ListingView> mine(@RequestHeader("X-User-Email") String email){return listings.findByProviderEmailIgnoreCaseAndStatus(email,"ACTIVE").stream().map(l->toView(l,null,null)).toList();}
    @GetMapping("/{id}") public ListingView get(@PathVariable UUID id){ResourceListing listing=find(id);if(!listing.getStatus().equals("ACTIVE"))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Listing not found");return toView(listing,null,null);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ListingView create(@Valid @RequestBody ListingRequest request,@RequestHeader("X-User-Email") String email){
        requireMember(request.communityId(),email);
        validateListing(request);
        ResourceListing listing=listings.save(new ResourceListing(request.title(),request.description(),request.category(),request.communityId(),request.communityName(),email,request.visibility().toUpperCase(),request.exchangeType().toUpperCase(),request.negotiable(),request.priceInr(),request.priceUnit().toUpperCase(),request.latitude(),request.longitude(),request.imageUrl(),request.availableFrom(),request.availableTo()));
        return toView(listing,null,null);
    }
    @PutMapping("/{id}") public ListingView update(@PathVariable UUID id,@Valid @RequestBody ListingRequest request,@RequestHeader("X-User-Email") String email){
        ResourceListing l=find(id);if(!l.getProviderEmail().equalsIgnoreCase(email))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the provider can edit this listing");validateListing(request);
        requireMember(l.getCommunityId(),email);
        l.update(request.title(),request.description(),request.category(),request.visibility().toUpperCase(),request.exchangeType().toUpperCase(),request.negotiable(),request.priceInr(),request.priceUnit().toUpperCase(),request.latitude(),request.longitude(),request.imageUrl(),request.availableFrom(),request.availableTo());return toView(listings.save(l),null,null);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void archive(@PathVariable UUID id,@RequestHeader("X-User-Email") String email){ResourceListing l=find(id);if(!l.getProviderEmail().equalsIgnoreCase(email))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the provider can archive this listing");l.archive();listings.save(l);}

    private ResourceListing find(UUID id){return listings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Listing not found"));}
    private void validateListing(ListingRequest request){
        validateTerms(request.exchangeType(),request.negotiable(),request.priceInr());
        if(!Set.of("COMMUNITY","PUBLIC").contains(request.visibility().toUpperCase()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Visibility must be COMMUNITY or PUBLIC");
        if(!"DAY".equalsIgnoreCase(request.priceUnit()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Price unit must be DAY");
        if(request.availableFrom()!=null&&request.availableFrom().isBefore(LocalDate.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Availability cannot start in the past");
        if(request.availableTo()!=null&&request.availableTo().isBefore(LocalDate.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Availability cannot end in the past");
        if(request.availableFrom()!=null&&request.availableTo()!=null&&request.availableTo().isBefore(request.availableFrom()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Availability end must be on or after the start date");
        if(request.imageUrl()!=null&&!request.imageUrl().isBlank()&&!request.imageUrl().matches("/api/borrowcircle/listings/images/[0-9a-fA-F-]{36}\\.(jpg|png)"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Image must be uploaded through the listing image endpoint");
    }
    private void requireMember(UUID communityId,String email){try{Boolean member=communities.get().uri("/api/communities/internal/{id}/members/{email}",communityId,email).header("X-Internal-Token",internalToken).retrieve().body(Boolean.class);if(!Boolean.TRUE.equals(member))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must belong to the listing community");}catch(ResponseStatusException ex){throw ex;}catch(Exception ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Community membership could not be verified",ex);}}
    private void validateTerms(String type,boolean negotiable,BigDecimal price){if(type==null||!Set.of("PAID","FREE").contains(type.toUpperCase()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Exchange type must be PAID or FREE");if(negotiable&&!type.equalsIgnoreCase("PAID"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Only paid listings can be open to negotiation");if(type.equalsIgnoreCase("PAID")&&(price==null||price.signum()<=0))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Paid listings need a positive INR price");if(type.equalsIgnoreCase("FREE")&&price!=null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Free listings cannot have a price");}
    private ListingView toView(ResourceListing l,BigDecimal lat,BigDecimal lon){Double distance=null;if(lat!=null&&lon!=null&&l.getLatitude()!=null&&l.getLongitude()!=null)distance=distance(lat.doubleValue(),lon.doubleValue(),l.getLatitude().doubleValue(),l.getLongitude().doubleValue());return new ListingView(l.getId(),l.getTitle(),l.getDescription(),l.getCategory(),l.getCommunityId(),l.getCommunityName(),l.getProviderEmail(),l.getVisibility(),"NEGOTIABLE".equalsIgnoreCase(l.getExchangeType())?"PAID":l.getExchangeType(),l.isNegotiable(),l.getPriceInr(),"INR",l.getPriceUnit(),l.getLatitude(),l.getLongitude(),l.getImageUrl(),l.getAvailableFrom(),l.getAvailableTo(),distance,l.getCreatedAt());}
    private double distance(double a,double b,double c,double d){double r=6371;double da=Math.toRadians(c-a),db=Math.toRadians(d-b);double h=Math.sin(da/2)*Math.sin(da/2)+Math.cos(Math.toRadians(a))*Math.cos(Math.toRadians(c))*Math.sin(db/2)*Math.sin(db/2);return Math.round((r*2*Math.atan2(Math.sqrt(h),Math.sqrt(1-h)))*10)/10d;}
    public record ListingRequest(@NotBlank @Size(max=140) String title,@NotBlank @Size(max=1600) String description,@NotBlank @Size(max=50) String category,@NotNull UUID communityId,@NotBlank @Size(max=100) String communityName,@NotBlank String visibility,@NotBlank String exchangeType,boolean negotiable,@DecimalMin(value="0.0") @Digits(integer=8,fraction=2) BigDecimal priceInr,@NotBlank String priceUnit,@DecimalMin(value="-90.0") @DecimalMax(value="90.0") BigDecimal latitude,@DecimalMin(value="-180.0") @DecimalMax(value="180.0") BigDecimal longitude,@Size(max=600) String imageUrl,LocalDate availableFrom,LocalDate availableTo){}
    public record ListingView(UUID id,String title,String description,String category,UUID communityId,String communityName,String providerEmail,String visibility,String exchangeType,boolean negotiable,BigDecimal price,String currency,String priceUnit,BigDecimal latitude,BigDecimal longitude,String imageUrl,LocalDate availableFrom,LocalDate availableTo,Double distanceKm,java.time.Instant createdAt){}
}
