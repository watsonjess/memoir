package com.makers.memoir.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "moments")
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private MomentType type;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "image_caption", length = 255)
    private String imageCaption;

    @Column(columnDefinition = "text")
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public enum MomentType {
        PHOTO, NOTE
    }
}
