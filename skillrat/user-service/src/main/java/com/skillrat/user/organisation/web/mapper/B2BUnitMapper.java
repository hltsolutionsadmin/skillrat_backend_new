package com.skillrat.user.organisation.web.mapper;

import com.skillrat.user.organisation.domain.Address;
import com.skillrat.user.organisation.domain.B2BGroup;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.web.dto.AddressDTO;
import com.skillrat.user.organisation.web.dto.B2BUnitDTO;

public class B2BUnitMapper {

    public static B2BUnitDTO toDTO(B2BUnit unit) {
        if (unit == null) return null;
        B2BUnitDTO dto = new B2BUnitDTO();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setType(unit.getType());
        dto.setStatus(unit.getStatus());
        dto.setContactEmail(unit.getContactEmail());
        dto.setContactPhone(unit.getContactPhone());
        dto.setWebsite(unit.getWebsite());
        dto.setAddress(toDTO(unit.getAddress()));
        B2BGroup group = unit.getGroup();
        if (group != null) {
            dto.setGroupId(group.getId());
            dto.setGroupName(group.getName());
        }
        return dto;
    }

    private static AddressDTO toDTO(Address address) {
        if (address == null) return null;
        AddressDTO dto = new AddressDTO();
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setPostalCode(address.getPostalCode());
        dto.setFullText(address.getFullText());
        return dto;
    }
}
