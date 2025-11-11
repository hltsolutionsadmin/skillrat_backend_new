package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "holiday_calendar")
@Getter
@Setter
@NoArgsConstructor
public class HolidayCalendar extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    // Optional: calendar scoped to a B2B unit
    private UUID b2bUnitId;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HolidayDay> days = new ArrayList<>();
}
