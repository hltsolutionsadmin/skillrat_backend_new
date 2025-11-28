package com.skillrat.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base response DTO that all response DTOs should extend.
 */
@Data
@JsonInclude(Include.NON_NULL)
public abstract class BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;
}
