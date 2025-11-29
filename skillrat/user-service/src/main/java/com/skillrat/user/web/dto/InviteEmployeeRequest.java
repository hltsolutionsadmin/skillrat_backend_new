package com.skillrat.user.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteEmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private List<UUID> roleIds;
}
