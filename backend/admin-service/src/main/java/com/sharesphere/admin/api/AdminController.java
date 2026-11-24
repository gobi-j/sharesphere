package com.sharesphere.admin.api;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/admin") public class AdminController {
 private final RestClient booking;private final String internalToken;
 public AdminController(@Value("${services.booking:http://localhost:8084}")String bookingUrl,@Value("${sharesphere.internal.token}")String internalToken){this.booking=RestClient.builder().baseUrl(bookingUrl).build();this.internalToken=internalToken;}
 @GetMapping("/overview") public Map<String,Object> overview(@RequestHeader("X-User-Role")String role){requireAdmin(role);List<Map<String,Object>> disputes=disputes();long open=disputes.stream().filter(d->"OPEN".equals(d.get("status"))).count();return Map.of("platform","ShareSphere","activeServices",List.of("auth","communities","borrowcircle","bookings","payments","chat","notifications","reviews","admin"),"openDisputes",open,"totalDisputes",disputes.size());}
 @GetMapping("/disputes") public List<Map<String,Object>> disputes(@RequestHeader("X-User-Role")String role){requireAdmin(role);return disputes();}
 @PostMapping("/disputes/{id}/resolve") public Map<?,?> resolve(@PathVariable UUID id,@RequestHeader("X-User-Role")String role,@RequestHeader("X-User-Email")String email,@RequestBody ResolveRequest request){requireAdmin(role);return booking.post().uri("/api/bookings/internal/disputes/{id}/resolve",id).header("X-Internal-Token",internalToken).body(Map.of("adminEmail",email,"resolution",request.resolution())).retrieve().body(Map.class);}
 private List<Map<String,Object>> disputes(){try{return booking.get().uri("/api/bookings/internal/disputes").header("X-Internal-Token",internalToken).retrieve().body(new ParameterizedTypeReference<>(){});}catch(Exception ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Booking service is unavailable",ex);}}
 private void requireAdmin(String role){if(!"ADMIN".equalsIgnoreCase(role))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform administrator role required");}
 public record ResolveRequest(String resolution){}
}
