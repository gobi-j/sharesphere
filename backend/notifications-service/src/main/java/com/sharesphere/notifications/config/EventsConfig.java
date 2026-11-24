package com.sharesphere.notifications.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class EventsConfig {
 @Bean TopicExchange sharesphereEvents(){return new TopicExchange("sharesphere.events",true,false);}
 @Bean Queue bookingEventsQueue(){return QueueBuilder.durable("notifications.booking-events").build();}
 @Bean Binding bookingEventsBinding(Queue bookingEventsQueue,TopicExchange sharesphereEvents){return BindingBuilder.bind(bookingEventsQueue).to(sharesphereEvents).with("booking.#");}
 @Bean Jackson2JsonMessageConverter eventMessageConverter(){return new Jackson2JsonMessageConverter();}
}
