package com.makers.memoir.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Getter
@Setter
@Table(name = "weekly")
public class Weekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "week_start", nullable = false)
    private LocalDateTime weekStart;

    @Column(name = "send_date", nullable = false)
    private LocalDateTime sendDate;

    @Column(nullable = false)
    private String status = "open"; // open / sent

    private String title;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "weekly", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<WeeklyMoment> weeklyMoments = new ArrayList<>();

    @Column(name = "pdf_url", length = 255)
    private String pdfUrl;

    @Column(name = "html_content", columnDefinition = "text")
    private String htmlContent;

    public Weekly() {}

    public Weekly(Group group, LocalDateTime weekStart, LocalDateTime sendDate) {
        this.group = group;
        this.weekStart = weekStart;
        this.sendDate = sendDate;
        this.status = "open";
    }
}