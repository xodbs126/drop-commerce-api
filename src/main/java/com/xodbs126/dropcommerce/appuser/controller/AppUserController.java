package com.xodbs126.dropcommerce.appuser.controller;


import com.xodbs126.dropcommerce.appuser.dto.AppUserRegisterRequestDto;
import com.xodbs126.dropcommerce.appuser.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;


    @PostMapping("/users")
    public ResponseEntity<Void> register(@RequestBody @Valid AppUserRegisterRequestDto requestDto) {
        appUserService.register(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
