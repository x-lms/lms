package com.example.lms.dto;

import lombok.Data;

@Data
public class NoticeFile {
	private int fileNo;
	private int noticeNo;
	private String fileName;
	private String originName;
	private String createdate;
}
