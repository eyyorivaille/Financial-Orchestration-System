package com.financial.project.user.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "user_profile")
@Getter
class UserProfile {

    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "email")
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
        // JPA
    }

    static UserProfile of(String customerId, String email, String displayName) {
        UserProfile profile = new UserProfile();
        profile.customerId = customerId;
        profile.email = email;
        profile.displayName = displayName;
        profile.updatedAt = Instant.now();
        return profile;
    }

    void update(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }
}
