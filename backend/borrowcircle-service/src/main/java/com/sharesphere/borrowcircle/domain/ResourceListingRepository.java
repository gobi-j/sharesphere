package com.sharesphere.borrowcircle.domain;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ResourceListingRepository extends JpaRepository<ResourceListing,UUID> { List<ResourceListing> findByProviderEmailIgnoreCaseAndStatus(String providerEmail,String status); }
