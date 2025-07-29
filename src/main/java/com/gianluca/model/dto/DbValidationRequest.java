package com.gianluca.dto;

import lombok.Data;

@Data
public class DbValidationRequest {
    private String dbAlias;
    private String tableName;
}