package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "holiday_day")
@Getter
@Setter
@NoArgsConstructor
public class HolidayDay extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "calendar_id")
    private HolidayCalendar calendar;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 150)
    private String name;

    private boolean optionalHoliday = false;
}
