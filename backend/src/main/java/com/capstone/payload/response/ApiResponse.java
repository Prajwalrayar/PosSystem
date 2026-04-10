package com.capstone.payload.response;

import lombok.Data;

@Data
public class ApiResponse {

    String message;

    public void setMessage(String message) {
        this.message = message;
    }
}
