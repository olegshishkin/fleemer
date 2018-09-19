package com.fleemer.service;

public interface UserAvailabilityService {
    boolean isOnline(long id);

    void setOnline(long id);

    void setOnline(String username);
}
