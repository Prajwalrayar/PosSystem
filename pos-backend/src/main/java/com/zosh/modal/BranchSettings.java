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
@Table(name = "branch_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchSettings {

    @Id
    private Long branchId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String printerJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String taxJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String paymentJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String discountJson;

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
