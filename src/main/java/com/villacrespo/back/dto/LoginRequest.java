package com.villacrespo.back.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginRequest(
        @JsonAlias({"username", "user"}) String usuario,
        @JsonAlias({"password"}) String contrasena
) {}