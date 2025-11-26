package com.example.lms.dto;

import java.util.List;
import lombok.Data;

@Data
public class StudentCourse {
		private int courseNo;
		private String courseName;
		private String empName;            // 교수명
		private String coursePeriod;
		private int currentCnt;           // 현재 수강인원
		private int maxCnt;               // 최대 인원
		private boolean isFull;           // 수강 가능 여부
		private String courseLocation;    // 대표 강의실
		private String courseDate;        // 강의요일
		private String courseTimeStart;   // 강의 시작 시간
		private String courseTimeEnd;     // 강의 끝나는 시간

//  강의 시간 상세 리스트 유지
private List<CourseTime> courseTimes;

}
