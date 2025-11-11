package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "wbs_element")
@Getter
@Setter
@NoArgsConstructor
public class WBSElement extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 64, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WBSCategory category = WBSCategory.OTHER;

    private LocalDate startDate;
    private LocalDate endDate;
}
