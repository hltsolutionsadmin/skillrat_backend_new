package com.skillrat.project.dto.response;

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
