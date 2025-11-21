package com.example.lms.dto;

import lombok.Data;

@Data
public class LoginUser {
	private String userId;
	private String userPw;
	private String userType;
}
