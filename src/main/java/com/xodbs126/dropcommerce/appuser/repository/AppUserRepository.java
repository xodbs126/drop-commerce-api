package com.xodbs126.dropcommerce.appuser.repository;

import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface AppUserRepository extends JpaRepository<AppUser, Long> {

}
