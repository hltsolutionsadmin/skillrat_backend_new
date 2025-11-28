package com.skillrat.user.organisation.web.dto;

import java.util.UUID;

import com.skillrat.user.organisation.domain.B2BUnitStatus;
import com.skillrat.user.organisation.domain.B2BUnitType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class B2BUnitDTO {
    private UUID id;
    private String name;
    private B2BUnitType type;
    private B2BUnitStatus status;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private AddressDTO address;
    private UUID groupId;
    private String groupName;
}
