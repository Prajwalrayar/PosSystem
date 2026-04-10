package com.zosh.service;

import com.zosh.payload.request.CreateExportRequest;
import com.zosh.payload.response.ExportJobResponse;

import java.util.UUID;

public interface ExportService {
    ExportJobResponse createExport(CreateExportRequest request);
    ExportJobResponse getExportStatus(UUID exportId);
    byte[] downloadExport(UUID exportId);
    String getExportFileName(UUID exportId);
}
