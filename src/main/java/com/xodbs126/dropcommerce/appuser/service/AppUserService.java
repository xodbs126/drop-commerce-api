package com.xodbs126.dropcommerce.appuser.service;

import com.xodbs126.dropcommerce.appuser.dto.AppUserRegisterRequestDto;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    public void register(AppUserRegisterRequestDto requestDto) {
        String username = requestDto.username();
        String hashedPassword = passwordEncoder.encode(requestDto.password());

        AppUser appUser = new AppUser(username, hashedPassword);
        appUserRepository.save(appUser);

    }
}
