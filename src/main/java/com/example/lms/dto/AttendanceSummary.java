package com.example.lms.dto;

import lombok.Data;

@Data
public class AttendanceSummary {
	private int studentNo;
	private int total;     // 총 출석일
	private int attendance; // 출석
    private int late;      // 지각
    private int absent;    // 결석
}
