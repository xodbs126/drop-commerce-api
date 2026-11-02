package com.xodbs126.dropcommerce.appuser.dto;

import jakarta.validation.constraints.NotNull;

public record AppUserLoginRequestDto(@NotNull String username, @NotNull String password) {
}
