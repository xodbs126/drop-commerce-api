package com.xodbs126.dropcommerce.storeapplication.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.xodbs126.dropcommerce.storeapplication.entity.ApplicationStatus.PENDING;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StoreApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appUserId;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String storeName;

    private String storeDescription;
    
    private LocalDateTime createdAt;

    public StoreApplication(Long appUserId, String storeName, String storeDescription, LocalDateTime createdAt) {
        this.appUserId = appUserId;
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.status = PENDING;
        this.createdAt = createdAt;
    }
}
