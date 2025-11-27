package com.skillrat.organisation.web.mapper;

import com.skillrat.organisation.domain.Address;
import com.skillrat.organisation.web.dto.AddressDTO;

public class OnboardingMapper {

    public static Address toEntity(AddressDTO dto) {
        if (dto == null) return null;
        Address addr = new Address();
        addr.setLine1(dto.getLine1());
        addr.setLine2(dto.getLine2());
        addr.setCity(dto.getCity());
        addr.setState(dto.getState());
        addr.setCountry(dto.getCountry());
        addr.setPostalCode(dto.getPostalCode());
        addr.setFullText(dto.getFullText());
        return addr;
    }
}
