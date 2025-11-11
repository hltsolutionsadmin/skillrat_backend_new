package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "time_entry_approval")
@Getter
@Setter
@NoArgsConstructor
public class TimeEntryApproval extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_entry_id")
    private TimeEntry timeEntry;

    @Column(nullable = false)
    private UUID approverId; // manager user/employee id

    @Column(nullable = false)
    private Instant approvedAt = Instant.now();

    @Column(length = 200)
    private String approverNote;
}
