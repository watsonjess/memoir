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
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String type = "weekly"; // weekly / event

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Weekly> weeklies = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Event> events = new ArrayList<>();

    **/

    public Group() {}

    public Group(String name, String type, User createdBy) {
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
    }
}
