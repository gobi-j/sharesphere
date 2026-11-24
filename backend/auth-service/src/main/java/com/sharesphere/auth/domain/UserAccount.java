package com.sharesphere.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(nullable = false, length = 100)
    private String fullName;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(length = 100)
    private String upiVpa;
    @Column(nullable = false, length = 20)
    private String role = "MEMBER";
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected UserAccount() {}
    public UserAccount(String email, String fullName, String passwordHash) {
        this.email = email.toLowerCase().trim(); this.fullName = fullName.trim(); this.passwordHash = passwordHash;
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getUpiVpa() { return upiVpa; }
    public void updateProfile(String fullName, String upiVpa) { this.fullName = fullName.trim(); this.upiVpa = upiVpa == null || upiVpa.isBlank() ? null : upiVpa.trim(); }
    public void assignRole(String role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
}
