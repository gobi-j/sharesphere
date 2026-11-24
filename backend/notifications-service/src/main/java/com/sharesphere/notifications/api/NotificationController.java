package com.sharesphere.notifications.api;
import com.sharesphere.notifications.domain.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/notifications") public class NotificationController {
 private final NotificationRepository repository;
 public NotificationController(NotificationRepository repository){this.repository=repository;}
 @GetMapping public List<NotificationView> mine(@RequestHeader("X-User-Email")String email){return repository.findTop100ByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(email).stream().map(this::view).toList();}
 @PostMapping("/{id}/read") public NotificationView read(@PathVariable UUID id,@RequestHeader("X-User-Email")String email){NotificationRecord n=repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));if(!n.getRecipientEmail().equalsIgnoreCase(email))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This notification belongs to another member");n.markRead();return view(repository.save(n));}
 private NotificationView view(NotificationRecord n){return new NotificationView(n.getId(),n.getEventType(),n.getMessage(),n.getReferenceId(),n.isRead(),n.getCreatedAt());}
 public record NotificationView(UUID id,String type,String message,String referenceId,boolean read,java.time.Instant createdAt){}
}
