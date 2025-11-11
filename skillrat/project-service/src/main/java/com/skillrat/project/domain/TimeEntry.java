package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "time_entry")
@Getter
@Setter
@NoArgsConstructor
public class TimeEntry extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wbs_id")
    private WBSElement wbsElement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private ProjectMember member;

    @Column(nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours; // e.g., 7.50

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TimeEntryStatus status = TimeEntryStatus.DRAFT;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "timeEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntryApproval> approvals = new ArrayList<>();
}
