package com.xodbs126.dropcommerce.appuser;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.dto.AppUserRegisterRequestDto;
import com.xodbs126.dropcommerce.appuser.service.AppUserService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@Disabled("실험 기록: 직접 실행해서 출력 확인용. assert 없음")
public class ConstraintNameExperimentTest extends IntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Test
    public void printConstraintNameWhenPasswordNull() {
        try {
            appUserService.register(new AppUserRegisterRequestDto("testuser1", null));
        } catch (DataIntegrityViolationException e) {
            System.out.println("e     = " + e.getClass().getName());
            Throwable cause = e.getCause();
            System.out.println("cause = " + (cause == null ? "null" : cause.getClass().getName()));
            if (cause instanceof ConstraintViolationException cve) {
                System.out.println("name  = [" + cve.getConstraintName() + "]");
            } else {
                System.out.println("name  = (ConstraintViolationException 아님)");
            }
        }
    }
}
