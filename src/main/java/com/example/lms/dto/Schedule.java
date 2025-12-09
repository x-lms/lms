package com.example.lms.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Schedule {
	private int scheduleNo;
	private int scheduleWriter;
	private String scheduleTitle;
	private String scheduleContents;
	private LocalDate scheduleStartDate;
	private LocalDate scheduleEndDate;
}
