package com.example.lms.dto;

import lombok.Data;

@Data
public class Schedule {
	private int scheduleNo;
	private int scheduleWriter;
	private String scheduleTitle;
	private String scheduleContents;
	private String scheduleData;
}
