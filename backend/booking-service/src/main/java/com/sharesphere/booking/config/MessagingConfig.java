package com.sharesphere.booking.config;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class MessagingConfig {
 @Bean TopicExchange shareSphereEvents(){return new TopicExchange("sharesphere.events",true,false);}
 @Bean Jackson2JsonMessageConverter eventMessageConverter(){return new Jackson2JsonMessageConverter();}
 @Bean RabbitTemplate rabbitTemplate(ConnectionFactory factory,Jackson2JsonMessageConverter converter){RabbitTemplate t=new RabbitTemplate(factory);t.setMessageConverter(converter);return t;}
}
