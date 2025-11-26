package com.skillrat.placement.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "openings")
@Getter
@Setter
@NoArgsConstructor
public class Opening extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpeningType type; // JOB or INTERNSHIP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpeningStatus status = OpeningStatus.OPEN;

    @Column(length = 200)
    private String location;

    @Column(name = "b2b_unit_id", nullable = false)
    private UUID b2bUnitId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;
}
