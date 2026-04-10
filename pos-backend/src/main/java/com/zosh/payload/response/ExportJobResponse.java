package com.zosh.payload.response;

import com.zosh.modal.ExportJobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ExportJobResponse {
    private UUID id;
    private ExportJobStatus status;
    private Integer progress;
    private LocalDateTime expiresAt;
}
