package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "wbs_allocation")
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
