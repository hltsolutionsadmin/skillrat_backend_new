package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "wbs_allocation",
        indexes = {
                @Index(name = "idx_alloc_member", columnList = "member_id"),
                @Index(name = "idx_alloc_wbs", columnList = "wbs_id"),
                @Index(name = "idx_alloc_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class WBSAllocation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private ProjectMember member;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wbs_id")
    private WBSElement wbsElement;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active = true;
}
