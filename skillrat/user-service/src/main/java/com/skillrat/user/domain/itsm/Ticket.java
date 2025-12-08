package com.skillrat.user.domain.itsm;

import com.skillrat.user.domain.User;
import com.skillrat.user.domain.itsm.TicketComment;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 20)
    private String priority;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(length = 50)
    private String assetId;

    @Column(length = 100)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketComment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        if (this.ticketNumber == null) {
            // Generate a ticket number like TKT-2023-0001
            String prefix = "TKT-" + LocalDateTime.now().getYear() + "-";
            // This is a simple implementation - in production, you'd want a more robust solution
            this.ticketNumber = prefix + String.format("%04d", (int)(Math.random() * 10000));
        }
    }

    public void addComment(TicketComment comment) {
        comments.add(comment);
        comment.setTicket(this);
    }

    public void removeComment(TicketComment comment) {
        comments.remove(comment);
        comment.setTicket(null);
    }
}
