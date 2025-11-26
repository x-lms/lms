package com.example.lms.dto;

import lombok.Data;

@Data
public class Attendance {
	private int attNo;
	private int studentNo;
	private int courseNo;
	private int empNo;
	private String attDate;
	private String attState;
}
