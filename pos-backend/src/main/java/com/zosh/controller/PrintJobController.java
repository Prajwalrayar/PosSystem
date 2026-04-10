package com.zosh.controller;

import com.zosh.payload.request.CreatePrintJobRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.PrintJobResponse;
import com.zosh.service.PrintJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/print/jobs")
@RequiredArgsConstructor
public class PrintJobController {

    private final PrintJobService printJobService;

    @PostMapping
    public ResponseEntity<ApiResponseBody<PrintJobResponse>> createPrintJob(@Valid @RequestBody CreatePrintJobRequest request) {
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Print job created", printJobService.createPrintJob(request)));
    }
}
