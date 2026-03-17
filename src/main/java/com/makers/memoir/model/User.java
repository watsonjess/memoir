package com.makers.memoir.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String profileImage;
    private java.time.LocalDateTime createdAt;
    private String firstname;
    private String lastname;
}