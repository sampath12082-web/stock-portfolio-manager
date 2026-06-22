package com.stocks.myportfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_groww_config")
public class UserGrowwConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "access_token_encrypted", columnDefinition = "TEXT")
    private String accessTokenEncrypted;

    @Column(name = "api_secret_encrypted", columnDefinition = "TEXT")
    private String apiSecretEncrypted;

    private boolean enabled = false;

    public UserGrowwConfig() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAccessTokenEncrypted() { return accessTokenEncrypted; }
    public void setAccessTokenEncrypted(String v) { this.accessTokenEncrypted = v; }
    public String getApiSecretEncrypted() { return apiSecretEncrypted; }
    public void setApiSecretEncrypted(String v) { this.apiSecretEncrypted = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
