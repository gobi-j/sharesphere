package com.sharesphere.gateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class RoutesConfig {
 @Bean RouteLocator routes(RouteLocatorBuilder builder,@Value("${services.auth:http://localhost:8081}")String auth,@Value("${services.community:http://localhost:8082}")String community,@Value("${services.borrowcircle:http://localhost:8083}")String borrow,@Value("${services.booking:http://localhost:8084}")String booking,@Value("${services.payments:http://localhost:8085}")String payments,@Value("${services.chat:http://localhost:8086}")String chat,@Value("${services.notifications:http://localhost:8087}")String notifications,@Value("${services.reviews:http://localhost:8088}")String reviews,@Value("${services.admin:http://localhost:8089}")String admin){return builder.routes().route("auth",r->r.path("/api/auth/**").uri(auth)).route("communities",r->r.path("/api/communities/**").uri(community)).route("borrowcircle",r->r.path("/api/borrowcircle/**").uri(borrow)).route("bookings",r->r.path("/api/bookings/**").uri(booking)).route("payments",r->r.path("/api/payments/**").uri(payments)).route("chat",r->r.path("/api/chat/**").uri(chat)).route("notifications",r->r.path("/api/notifications/**").uri(notifications)).route("reviews",r->r.path("/api/reviews/**").uri(reviews)).route("admin",r->r.path("/api/admin/**").uri(admin)).build();}
}
