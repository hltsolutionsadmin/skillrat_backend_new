package com.skillrat.project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectClientDTO {
    private String name;
    private String primaryContactEmail;
    private String secondaryContactEmail;
}
