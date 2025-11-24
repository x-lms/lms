package com.example.lms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.dto.Course;
import com.example.lms.dto.Emp;
import com.example.lms.mapper.ProfessorMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfessorService {
	@Autowired
	ProfessorMapper professorMapper;
	
	// 강의 상세보기
	public Course getCourseOne(int courseNo) {
		return professorMapper.courseOne(courseNo);
	}
	
	// 강의 등록
	public int insertCourse(Course c) {
		return professorMapper.insertCourse(c);		
	}
	
	// 강의 수정
	public int updateCourse(Course c) {
		return professorMapper.updateCourse(c);		
	}
	
	// 강의 삭제
	public int deleteCourse(int courseNo) {
		return professorMapper.deleteCourse(courseNo);		
	}
		
	// 강의리스트
	public List<Course> courseListByPage(int empNo, int currentPage, String searchWord) {
		int rowPerPage = 10;
		int beginRow = (currentPage - 1) * rowPerPage;
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		m.put("rowPerPage", rowPerPage);
		m.put("beginRow", beginRow);
		m.put("searchWord", searchWord);
		List<Course> courseList = professorMapper.courseListByPage(m);
		log.debug("courseList : {}", courseList);
		
		return courseList;
	}
	
	public int getCourseCount(int empNo, String searchWord) {
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		m.put("searchWord", searchWord);
		return professorMapper.getCourseCount(m);
	}
	
	
	// 내 정보 수정 
	public int updateProfessorInfo(Emp e) {
		return professorMapper.updateProfessorInfo(e);
	}
	
	// 내 정보
	public Emp professorInfo(int empNo) {
		return professorMapper.professorInfo(empNo);
	}



	
}
