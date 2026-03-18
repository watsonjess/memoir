package com.makers.memoir.model;

import jakarta.persistence.*;
import lombok.*;
import com.makers.memoir.model.Weekly;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "weekly_moments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"weekly_id", "moment_id"}))
public class WeeklyMoment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "weekly_id", nullable = false)
    private Weekly weekly;

    @ManyToOne
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}