package com.zosh.controller;

import com.zosh.payload.request.CreateExportRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.ExportJobResponse;
import com.zosh.service.ExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @PostMapping
    public ResponseEntity<ApiResponseBody<ExportJobResponse>> createExport(@Valid @RequestBody CreateExportRequest request) {
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Export job created", exportService.createExport(request)));
    }

    @GetMapping("/{exportId}")
    public ResponseEntity<ApiResponseBody<ExportJobResponse>> getExportStatus(@PathVariable UUID exportId) {
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Export status fetched", exportService.getExportStatus(exportId)));
    }

    @GetMapping("/{exportId}/download")
    public ResponseEntity<byte[]> downloadExport(@PathVariable UUID exportId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(exportService.getExportFileName(exportId))
                        .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(exportService.downloadExport(exportId));
    }
}
