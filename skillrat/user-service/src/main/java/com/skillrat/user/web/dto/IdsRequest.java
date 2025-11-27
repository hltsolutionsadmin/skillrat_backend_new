package com.skillrat.user.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public class IdsRequest {
    @NotEmpty public List<UUID> ids;
}
