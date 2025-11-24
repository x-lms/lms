package com.example.lms.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Emp {
	private int empNo;
	private int deptNo; 
	private int empRole;
	private String empPw;
	private String empName;
	private String empEmail;
	private String empBirth;
	private String empPhone;
	private String empImg; // DTO에 MultipartFile로 선언
	
}
