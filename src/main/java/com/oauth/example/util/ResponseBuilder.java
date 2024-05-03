package com.oauth.example.util;

import com.oauth.example.domain.model.ApiResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor
public class ResponseBuilder<T> {
    private T body;
    private String msg = null;

    private HttpStatus httpStatus = HttpStatus.OK;

    public ResponseEntity<ApiResponse<T>> build() {
        return new ResponseEntity<>(new ApiResponse<>(this.body, this.msg), this.httpStatus);
    }

    public ResponseBuilder<T> httpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ResponseBuilder<T> body(T body) {
        this.body = body;
        return this;
    }

    public ResponseBuilder<T> msg(String msg) {
        this.msg = msg;
        return this;
    }
}
