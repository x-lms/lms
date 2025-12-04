package com.example.lms.dto;

import java.util.List;

import lombok.Data;

@Data
public class CourseWithTime {
	 private Course course;
	 private List<CourseTime> courseTimes;
}
