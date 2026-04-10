package com.zosh.service;

import com.zosh.exception.ResourceNotFoundException;
import com.zosh.payload.request.InitiateReturnRequest;
import com.zosh.payload.response.ReturnInitiationResponse;

public interface ReturnService {
    ReturnInitiationResponse initiateReturn(InitiateReturnRequest request) throws ResourceNotFoundException;
}
