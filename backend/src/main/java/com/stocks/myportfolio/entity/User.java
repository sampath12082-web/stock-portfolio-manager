package com.stocks.myportfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "security_question_1")
    private String securityQuestion1;

    @Column(name = "security_answer_1_hash")
    private String securityAnswer1Hash;

    @Column(name = "security_question_2")
    private String securityQuestion2;

    @Column(name = "security_answer_2_hash")
    private String securityAnswer2Hash;

    public User() {}

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getSecurityQuestion1() { return securityQuestion1; }
    public void setSecurityQuestion1(String securityQuestion1) { this.securityQuestion1 = securityQuestion1; }
    public String getSecurityAnswer1Hash() { return securityAnswer1Hash; }
    public void setSecurityAnswer1Hash(String hash) { this.securityAnswer1Hash = hash; }
    public String getSecurityQuestion2() { return securityQuestion2; }
    public void setSecurityQuestion2(String securityQuestion2) { this.securityQuestion2 = securityQuestion2; }
    public String getSecurityAnswer2Hash() { return securityAnswer2Hash; }
    public void setSecurityAnswer2Hash(String hash) { this.securityAnswer2Hash = hash; }
}
