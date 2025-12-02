package com.example.lms.dto;

import lombok.Data;

@Data
public class AjaxResult {
	private boolean success;
    private String message;
    private Object data;
}
