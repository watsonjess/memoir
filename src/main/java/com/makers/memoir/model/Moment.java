package com.makers.memoir.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "moments")
@Check(name = "check_photo", constraints = "type != 'PHOTO' OR image_url IS NOT NULL")
@Check(name = "check_note",  constraints = "type != 'NOTE'  OR content IS NOT NULL")
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

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
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MomentType {
        PHOTO, NOTE
    }
}
