package com.makers.memoir.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "memories")
public class Memory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "pin_x")
    private Double pinX = 0.0;

    @Column(name = "pin_y")
    private Double pinY = 0.0;

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<MemoryMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Thought> thoughts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}