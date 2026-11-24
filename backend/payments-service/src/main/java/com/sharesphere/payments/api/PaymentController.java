package com.sharesphere.payments.api;
import com.sharesphere.payments.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
@RestController @RequestMapping("/api/payments")
public class PaymentController {
 private final PaymentIntentRepository payments;private final String payeeName;private final String webhookSecret;private final String internalToken;private final RestClient booking;private final RestClient auth;private final ObjectMapper objectMapper;
 public PaymentController(PaymentIntentRepository payments,@Value("${sharesphere.payments.payee-name:ShareSphere}") String payeeName,@Value("${sharesphere.payments.webhook-secret:}")String webhookSecret,@Value("${sharesphere.internal.token}")String internalToken,@Value("${services.booking:http://localhost:8084}")String bookingUrl,@Value("${services.auth:http://localhost:8081}")String authUrl,ObjectMapper objectMapper){this.payments=payments;this.payeeName=payeeName;this.webhookSecret=webhookSecret;this.internalToken=internalToken;this.booking=RestClient.builder().baseUrl(bookingUrl).build();this.auth=RestClient.builder().baseUrl(authUrl).build();this.objectMapper=objectMapper;}
 @PostMapping("/upi/intents") @ResponseStatus(HttpStatus.CREATED) public IntentView create(@Valid @RequestBody CreateIntent req,@RequestHeader("X-User-Email") String payer){
  Map<?,?> details=booking.get().uri("/api/bookings/internal/{id}/payment-details",req.bookingId()).header("X-Internal-Token",internalToken).retrieve().body(Map.class);
  if(!payer.equalsIgnoreCase(String.valueOf(details.get("requesterEmail"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the requester can pay for this booking");
  if(!"PENDING_PAYMENT".equals(details.get("status")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking is not awaiting payment");
  BigDecimal amount=new BigDecimal(String.valueOf(details.get("amountInr")));String payeeEmail=String.valueOf(details.get("providerEmail"));
  Map<?,?> profile=auth.get().uri("/api/auth/internal/users/{email}/payment-profile",payeeEmail).header("X-Internal-Token",internalToken).retrieve().body(Map.class);
  String payeeVpa=String.valueOf(profile.get("upiVpa"));if(payeeVpa.equals("null")||payeeVpa.isBlank())throw new ResponseStatusException(HttpStatus.CONFLICT,"Provider has not added a UPI ID yet");
  PaymentIntent intent=payments.findByBookingId(req.bookingId()).orElseGet(()->payments.save(new PaymentIntent(req.bookingId(),payer,payeeEmail,payeeVpa,amount)));
  if(!intent.getPayerEmail().equalsIgnoreCase(payer)||intent.getAmountInr().compareTo(amount)!=0||!intent.getPayeeVpa().equalsIgnoreCase(payeeVpa))throw new ResponseStatusException(HttpStatus.CONFLICT,"This booking already has a different payment intent");
  String uri="upi://pay?pa="+enc(intent.getPayeeVpa())+"&pn="+enc(payeeName)+"&am="+intent.getAmountInr().toPlainString()+"&cu=INR&tn="+enc("ShareSphere booking "+intent.getBookingId());
  return new IntentView(intent.getId(),intent.getBookingId(),intent.getAmountInr(),"INR",intent.getStatus(),uri,intent.getCreatedAt());
 }
 @PostMapping("/webhooks/upi") public WebhookResponse upiWebhook(@RequestHeader("X-ShareSphere-Signature")String signature,@RequestBody String rawBody){
  if(webhookSecret.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"UPI webhook verification is not configured");
  if(!verifySignature(rawBody,signature))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid payment webhook signature");
  final ProviderEvent event;try{event=objectMapper.readValue(rawBody,ProviderEvent.class);}catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid payment event body",ex);}
  if(!Set.of("SUCCESS","COMPLETED","PAID").contains(event.status().toUpperCase()))return new WebhookResponse("IGNORED",event.paymentReference());
  PaymentIntent intent=payments.findById(event.paymentReference()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment reference not found"));
  if(!intent.getBookingId().equals(event.bookingId())||intent.getAmountInr().compareTo(event.amountInr())!=0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment event does not match the saved intent");
  if(!"PAID".equals(intent.getStatus())){try{booking.post().uri("/api/bookings/internal/{id}/payment-confirmed",intent.getBookingId()).header("X-Internal-Token",internalToken).retrieve().toBodilessEntity();}catch(Exception ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Booking could not be updated; provider may retry the event",ex);}intent.markPaid(event.transactionRef());payments.save(intent);}
  return new WebhookResponse("PAID",intent.getId());
 }
 @GetMapping("/upi/intents/{id}") public IntentView get(@PathVariable UUID id,@RequestHeader("X-User-Email") String email){PaymentIntent i=payments.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment not found"));if(!i.getPayerEmail().equalsIgnoreCase(email)&&!i.getPayeeEmail().equalsIgnoreCase(email))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Payment participants only");String uri="upi://pay?pa="+enc(i.getPayeeVpa())+"&pn="+enc(payeeName)+"&am="+i.getAmountInr().toPlainString()+"&cu=INR&tn="+enc("ShareSphere booking "+i.getBookingId());return new IntentView(i.getId(),i.getBookingId(),i.getAmountInr(),"INR",i.getStatus(),uri,i.getCreatedAt());}
 private String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
 private boolean verifySignature(String body,String signature){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));byte[] expected=mac.doFinal(body.getBytes(StandardCharsets.UTF_8));String provided=signature.startsWith("sha256=")?signature.substring(7):signature;return MessageDigest.isEqual(expected,HexFormat.of().parseHex(provided));}catch(Exception ex){return false;}}
 public record CreateIntent(@NotNull UUID bookingId){}
 public record ProviderEvent(UUID paymentReference,UUID bookingId,String transactionRef,BigDecimal amountInr,String status){}
 public record WebhookResponse(String status,UUID paymentReference){}
 public record IntentView(UUID id,UUID bookingId,BigDecimal amount,String currency,String status,String upiUri,java.time.Instant createdAt){}
}
