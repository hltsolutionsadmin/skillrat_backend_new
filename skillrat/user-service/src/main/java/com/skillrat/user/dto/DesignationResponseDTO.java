package com.skillrat.user.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationResponseDTO {

    private UUID designationId;
    private String designationName;

    private UUID bandId;
    private String bandName;
    private Integer experienceMin;
    private Integer experienceMax;
    private Double salary;

    private Long resourceCount; // Number of employees in this band for the given b2bUnit
}
