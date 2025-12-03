package com.example.lms.dto;

import lombok.Data;
import java.util.*;

@Data
public class StudentExcelResult {
	private int totalCount;
	private int successCount;
	private int failCount;
	private List<Student> successList = new ArrayList<>();
	private List<FailItem> failList = new ArrayList<>();
	
	@Data
	public static class FailItem {
		private int rowNum;
		private String studentNo;
		private String studentName;
		private String reason;
	}
}
