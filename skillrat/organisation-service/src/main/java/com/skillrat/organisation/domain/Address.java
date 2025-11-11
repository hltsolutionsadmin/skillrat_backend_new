package com.skillrat.organisation.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
public class Address extends BaseEntity {

    @Column(length = 128)
    private String line1;

    @Column(length = 128)
    private String line2;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String state;

    @Column(length = 64)
    private String country;

    @Column(length = 16)
    private String postalCode;

    @Column(length = 512)
    private String fullText;
}
