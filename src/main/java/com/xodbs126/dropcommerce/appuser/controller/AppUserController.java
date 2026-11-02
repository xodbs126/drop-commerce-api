package com.xodbs126.dropcommerce.appuser.controller;


import com.xodbs126.dropcommerce.appuser.dto.AppUserLoginRequestDto;
import com.xodbs126.dropcommerce.appuser.dto.AppUserRegisterRequestDto;
import com.xodbs126.dropcommerce.appuser.service.AppUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    private static final String SESSION_KEY_APP_USER_ID = "appUserId";

    private final AppUserService appUserService;

    @PostMapping("/users")
    public ResponseEntity<Void> register(@RequestBody @Valid AppUserRegisterRequestDto requestDto) {
        appUserService.register(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid AppUserLoginRequestDto loginRequestDto,
                                      HttpServletRequest request) {
        Long appUserId = appUserService.login(loginRequestDto);

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        HttpSession session = request.getSession();
        session.setAttribute(SESSION_KEY_APP_USER_ID, appUserId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
