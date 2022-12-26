package com.microtimemanagement.apiservice.converter;

public interface BaseDTOConverter<Base, BaseDTO> {

    public Base fromDTO(BaseDTO baseDTO);

    public BaseDTO toDTO(Base base);

}
