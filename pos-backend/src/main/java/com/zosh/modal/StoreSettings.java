package com.zosh.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreSettings {

    @Id
    private Long storeId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "store_id")
    private Store store;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String storeJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notificationJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String securityJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String paymentJson;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
