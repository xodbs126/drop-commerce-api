package com.xodbs126.dropcommerce.appuser.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AppUserRegisterRequestDto(
                                @Size(min = 8, max = 50)
                                @NotNull
                                @Pattern(regexp = "^[a-z0-9]+$")
                                String username,

                                @Size(min = 8, max = 64)
                                @NotNull
                                @Pattern(regexp = "^[\\x21-\\x7E]+$") String password) {
}
