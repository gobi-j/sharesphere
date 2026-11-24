package com.sharesphere.auth.api;

import com.sharesphere.auth.domain.UserAccount;
import com.sharesphere.auth.domain.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final long tokenMinutes;
    private final Set<String> adminEmails;
    private final String internalToken;

    public AuthController(UserAccountRepository users, PasswordEncoder passwords, JwtEncoder jwtEncoder,
                          @Value("${sharesphere.jwt.ttl-minutes:720}") long tokenMinutes,
                          @Value("${sharesphere.admin.emails:}") String adminEmails,
                          @Value("${sharesphere.internal.token}") String internalToken) {
        this.users = users; this.passwords = passwords; this.jwtEncoder = jwtEncoder; this.tokenMinutes = tokenMinutes;
        this.adminEmails = Arrays.stream(adminEmails.split(",")).map(String::trim).filter(s -> !s.isBlank()).map(String::toLowerCase).collect(java.util.stream.Collectors.toUnmodifiableSet());
        this.internalToken = internalToken;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        UserAccount user = new UserAccount(email, request.fullName(), passwords.encode(request.password()));
        if(adminEmails.contains(email)) user.assignRole("ADMIN");
        user = users.save(user);
        return issue(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
                .filter(value -> passwords.matches(request.password(), value.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
        return issue(user);
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("service", "auth", "status", "ready"); }

    @GetMapping("/profile") public ProfileView profile(@RequestHeader("X-User-Email") String email) { return users.findByEmailIgnoreCase(email).map(this::profileView).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")); }
    @PutMapping("/profile") public ProfileView updateProfile(@RequestHeader("X-User-Email") String email, @Valid @RequestBody ProfileRequest request) { UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")); user.updateProfile(request.fullName(), request.upiVpa()); return profileView(users.save(user)); }
    @GetMapping("/internal/users/{email}/payment-profile") public PaymentProfile paymentProfile(@PathVariable String email, @RequestHeader("X-Internal-Token") String token) { if (!internalToken.equals(token)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal service authentication failed"); UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")); return new PaymentProfile(user.getEmail(), user.getFullName(), user.getUpiVpa()); }
    private ProfileView profileView(UserAccount user) { return new ProfileView(user.getId().toString(), user.getEmail(), user.getFullName(), user.getRole(), user.getUpiVpa()); }

    private AuthResponse issue(UserAccount user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("sharesphere-auth").issuedAt(now).expiresAt(now.plusSeconds(tokenMinutes * 60))
                .subject(user.getEmail()).claim("userId", user.getId().toString()).claim("name", user.getFullName())
                .claim("role", user.getRole()).build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", tokenMinutes * 60, new UserView(user.getId().toString(), user.getEmail(), user.getFullName(), user.getRole()));
    }

    public record RegisterRequest(@NotBlank @Email @Size(max=254) String email,
                                 @NotBlank @Size(min=8, max=72) String password,
                                 @NotBlank @Size(min=2, max=100) String fullName) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserView user) {}
    public record UserView(String id, String email, String fullName, String role) {}
    public record ProfileRequest(@NotBlank @Size(min = 2, max = 100) String fullName, @Size(max = 100) String upiVpa) {}
    public record ProfileView(String id, String email, String fullName, String role, String upiVpa) {}
    public record PaymentProfile(String email, String fullName, String upiVpa) {}
}
