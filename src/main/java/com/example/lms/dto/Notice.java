package com.example.lms.dto;

import lombok.Data;

@Data
public class Notice {
	private int noticeNo;
	private int empNo;
	private String noticeWriter;
	private String noticeTitle;
	private String noticeContents;
	private String createdate;
}
