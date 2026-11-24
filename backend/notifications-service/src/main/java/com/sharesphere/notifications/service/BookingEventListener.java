package com.sharesphere.notifications.service;
import com.sharesphere.notifications.domain.*;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component public class BookingEventListener {
 private final NotificationRepository notifications;
 public BookingEventListener(NotificationRepository notifications){this.notifications=notifications;}
 @RabbitListener(queues="notifications.booking-events") public void onBookingEvent(Map<String,Object> event){
  String type=String.valueOf(event.getOrDefault("eventType","booking.update"));String booking=String.valueOf(event.getOrDefault("bookingId",""));String actor=String.valueOf(event.getOrDefault("actorEmail",""));
  String requester=String.valueOf(event.getOrDefault("requesterEmail",""));String provider=String.valueOf(event.getOrDefault("providerEmail",""));String reason=String.valueOf(event.getOrDefault("reason",""));
  String message=switch(type){case "booking.requested"->"You have a new BorrowCircle request.";case "booking.offer"->"A new offer was made on a BorrowCircle request.";case "booking.approved"->"A BorrowCircle request was approved.";case "booking.declined"->"A BorrowCircle request was declined.";case "booking.handover"->"The BorrowCircle handover was recorded.";case "booking.completed"->"A BorrowCircle exchange was completed.";default->"Your BorrowCircle booking was updated.";};
  if(!requester.isBlank()&&!requester.equalsIgnoreCase(actor))notifications.save(new NotificationRecord(requester,type,message+shortReason(reason),booking));
  if(!provider.isBlank()&&!provider.equalsIgnoreCase(actor))notifications.save(new NotificationRecord(provider,type,message+shortReason(reason),booking));
 }
 private String shortReason(String reason){return reason==null||reason.isBlank()?"":" Reason: "+reason.substring(0,Math.min(120,reason.length()));}
}
