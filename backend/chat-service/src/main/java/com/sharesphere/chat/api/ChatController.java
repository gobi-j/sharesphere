package com.sharesphere.chat.api;
import com.sharesphere.chat.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
@RestController @RequestMapping("/api/chat") public class ChatController {
 private final ChatMessageRepository messages;private final RestClient bookings;private final String internalToken;
 private final ConcurrentHashMap<UUID,CopyOnWriteArrayList<SseEmitter>> streams=new ConcurrentHashMap<>();
 public ChatController(ChatMessageRepository messages,@Value("${services.booking:http://localhost:8084}")String bookingUrl,@Value("${sharesphere.internal.token}")String internalToken){this.messages=messages;this.bookings=RestClient.builder().baseUrl(bookingUrl).build();this.internalToken=internalToken;}
 @GetMapping("/{bookingId}/messages") public List<MessageView> messages(@PathVariable UUID bookingId,@RequestHeader("X-User-Email")String email){requireParticipant(bookingId,email);return messages.findTop100ByBookingIdOrderBySentAtAsc(bookingId).stream().map(this::view).toList();}
 @PostMapping("/{bookingId}/messages") @ResponseStatus(HttpStatus.CREATED) public MessageView send(@PathVariable UUID bookingId,@RequestHeader("X-User-Email")String email,@Valid @RequestBody SendMessage request){requireParticipant(bookingId,email);ChatMessage saved=messages.save(new ChatMessage(bookingId,email,request.content()));MessageView view=view(saved);publish(bookingId,view);return view;}
 @GetMapping(value="/{bookingId}/stream",produces=MediaType.TEXT_EVENT_STREAM_VALUE) public SseEmitter stream(@PathVariable UUID bookingId,@RequestHeader("X-User-Email")String email){requireParticipant(bookingId,email);SseEmitter emitter=new SseEmitter(0L);CopyOnWriteArrayList<SseEmitter> clients=streams.computeIfAbsent(bookingId,key->new CopyOnWriteArrayList<>());clients.add(emitter);emitter.onCompletion(()->remove(bookingId,emitter));emitter.onTimeout(()->remove(bookingId,emitter));emitter.onError(error->remove(bookingId,emitter));try{emitter.send(SseEmitter.event().name("ready").data(Map.of("status","connected")));}catch(Exception ex){remove(bookingId,emitter);}return emitter;}
 private void publish(UUID bookingId,MessageView view){var clients=streams.get(bookingId);if(clients==null)return;for(SseEmitter emitter:clients){try{emitter.send(SseEmitter.event().name("message").data(view));}catch(Exception ex){remove(bookingId,emitter);}}}
 private void remove(UUID bookingId,SseEmitter emitter){var clients=streams.get(bookingId);if(clients!=null){clients.remove(emitter);if(clients.isEmpty())streams.remove(bookingId,clients);}}
 private void requireParticipant(UUID id,String email){try{Map<?,?> details=bookings.get().uri("/api/bookings/internal/{id}/payment-details",id).header("X-Internal-Token",internalToken).retrieve().body(Map.class);if(!email.equalsIgnoreCase(String.valueOf(details.get("requesterEmail")))&&!email.equalsIgnoreCase(String.valueOf(details.get("providerEmail"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only booking participants can access this chat");}catch(ResponseStatusException ex){throw ex;}catch(Exception ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found or unavailable",ex);}}
 private MessageView view(ChatMessage m){return new MessageView(m.getId(),m.getBookingId(),m.getSenderEmail(),m.getContent(),m.getSentAt());}
 public record SendMessage(@NotBlank @Size(max=2000)String content){}
 public record MessageView(UUID id,UUID bookingId,String senderEmail,String content,java.time.Instant sentAt){}
}
