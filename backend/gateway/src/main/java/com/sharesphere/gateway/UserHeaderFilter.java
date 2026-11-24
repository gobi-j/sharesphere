package com.sharesphere.gateway;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component public class UserHeaderFilter implements GlobalFilter,Ordered {
 @Override public int getOrder(){return -100;}
 @Override public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){return exchange.getPrincipal().cast(JwtAuthenticationToken.class).flatMap(auth->{var request=exchange.getRequest().mutate().headers(h->{h.remove("X-User-Email");h.remove("X-User-Role");h.add("X-User-Email",auth.getToken().getSubject());h.add("X-User-Role",auth.getToken().getClaimAsString("role"));}).build();return chain.filter(exchange.mutate().request(request).build());}).switchIfEmpty(chain.filter(exchange));}
}
