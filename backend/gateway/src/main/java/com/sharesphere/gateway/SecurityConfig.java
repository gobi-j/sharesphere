package com.sharesphere.gateway;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
@Configuration public class SecurityConfig {
 @Bean SecurityWebFilterChain gatewaySecurity(ServerHttpSecurity http){return http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(auth->auth.pathMatchers("/api/communities/internal/**","/api/bookings/internal/**","/api/auth/internal/**").denyAll().pathMatchers(HttpMethod.GET,"/api/borrowcircle/listings/images/**").permitAll().pathMatchers("/api/auth/register","/api/auth/login","/api/auth/health","/api/payments/webhooks/**","/actuator/**","/swagger-ui/**","/v3/api-docs/**").permitAll().pathMatchers(HttpMethod.OPTIONS,"/**").permitAll().anyExchange().authenticated()).oauth2ResourceServer(oauth->oauth.jwt(jwt->{})).build();}
 @Bean ReactiveJwtDecoder jwtDecoder(@Value("${sharesphere.jwt.secret}") String secret){SecretKey key=new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),"HmacSHA256");return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();}
}
