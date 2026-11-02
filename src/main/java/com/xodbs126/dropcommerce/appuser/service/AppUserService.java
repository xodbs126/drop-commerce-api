package com.xodbs126.dropcommerce.appuser.service;

import com.xodbs126.dropcommerce.appuser.dto.AppUserLoginRequestDto;
import com.xodbs126.dropcommerce.appuser.dto.AppUserRegisterRequestDto;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.exception.AppUserException;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.xodbs126.dropcommerce.appuser.exception.AppUserErrorCode.LOGIN_FAILED;
import static com.xodbs126.dropcommerce.appuser.exception.AppUserErrorCode.USERNAME_DUPLICATED;

@Transactional
@RequiredArgsConstructor
@Service
public class AppUserService {

    private static final String USERNAME_UNIQUE_CONSTRAINT = "uk_app_user_username";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(AppUserRegisterRequestDto requestDto) {
        String username = requestDto.username();
        String hashedPassword = passwordEncoder.encode(requestDto.password());

        AppUser appUser = new AppUser(username, hashedPassword);

        try {
            appUserRepository.save(appUser);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cve
                    && USERNAME_UNIQUE_CONSTRAINT.equals(cve.getConstraintName())) {
                throw new AppUserException(USERNAME_DUPLICATED, e);
            }
            throw e;
        }

    }

    @Transactional(readOnly = true)
    public Long login(AppUserLoginRequestDto appUserLoginRequestDto) {

        String username = appUserLoginRequestDto.username();

        AppUser loginUser = appUserRepository.findAppUserByUsername(username)
                .orElseThrow(() -> new AppUserException(LOGIN_FAILED));

        if (passwordEncoder.matches(appUserLoginRequestDto.password(), loginUser.getPassword())) {
            return loginUser.getId();
        } else {
            throw new AppUserException(LOGIN_FAILED);
        }
    }
}
