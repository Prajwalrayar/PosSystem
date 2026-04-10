package com.zosh.payload.response;

import com.zosh.modal.PrintJobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PrintJobResponse {
    private UUID id;
    private String type;
    private String referenceId;
    private String printerId;
    private PrintJobStatus status;
    private LocalDateTime createdAt;
}
