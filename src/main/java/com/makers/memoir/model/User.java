package com.makers.memoir.model;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 255)
    private String firstname;

    @Column(length = 255)
    private String lastname;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Moment> items = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<GroupMember> groupMemberships = new ArrayList<>();

}