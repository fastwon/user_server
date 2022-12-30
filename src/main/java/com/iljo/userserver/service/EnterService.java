package com.iljo.userserver.service;

import com.iljo.userserver.dto.EnterDto;

public interface EnterService {
    EnterDto enterRoom(String userId, EnterDto enterDto);

    EnterDto getEntersByUserId(String userId);

    void leaveTheRoom(String userId);

}





