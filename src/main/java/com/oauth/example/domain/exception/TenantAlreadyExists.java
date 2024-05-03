package com.oauth.example.domain.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantAlreadyExists extends RuntimeException {
}
