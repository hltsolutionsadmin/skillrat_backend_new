package com.skillrat.project.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * Base request DTO that all request DTOs should extend.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
