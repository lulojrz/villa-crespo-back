package com.villacrespo.back.dto;

public record LoginRequest(
        String username,
        String password
) {}