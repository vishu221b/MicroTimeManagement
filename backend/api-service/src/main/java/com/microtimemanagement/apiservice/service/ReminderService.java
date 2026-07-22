package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.ReminderDTO;

import java.util.List;

public interface ReminderService {

    ReminderDTO create(ReminderDTO dto);

    List<ReminderDTO> listForCurrentUser();

    ReminderDTO getById(String id);

    ReminderDTO update(String id, ReminderDTO dto);

    void softDelete(String id);
}
