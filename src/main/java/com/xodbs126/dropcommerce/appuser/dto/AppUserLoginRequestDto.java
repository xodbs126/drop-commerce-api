package com.xodbs126.dropcommerce.appuser.dto;

import jakarta.validation.constraints.NotBlank;

public record AppUserLoginRequestDto(@NotBlank String username, @NotBlank String password) {
}
