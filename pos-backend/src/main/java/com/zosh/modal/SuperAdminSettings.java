package com.zosh.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "super_admin_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminSettings {

    public static final Long SETTINGS_ID = 1L;

    @Id
    private Long id = SETTINGS_ID;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notificationJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String systemJson;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = SETTINGS_ID;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
