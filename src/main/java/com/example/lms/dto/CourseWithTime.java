package com.example.lms.dto;

import java.util.List;

import lombok.Data;

@Data
public class CourseWithTime {
	private Course course; // 기존 강의 정보
    private Course newCourse = new Course(); // 폼 입력용, 초기화 필수
    private List<CourseTime> courseTimes;
}
