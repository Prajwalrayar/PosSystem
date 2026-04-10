package com.zosh.service;

import com.zosh.payload.request.CreatePrintJobRequest;
import com.zosh.payload.response.PrintJobResponse;

public interface PrintJobService {
    PrintJobResponse createPrintJob(CreatePrintJobRequest request);
}
