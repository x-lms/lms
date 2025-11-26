package com.example.lms.dto;

import lombok.Data;

@Data
public class AttendanceHistory {
	private int historyNo;
	private int attNo;
	private String stateBefore;
	private String stateAfter;
	private String historyComment;
	private String historyFile;
	private String createdate;
}
