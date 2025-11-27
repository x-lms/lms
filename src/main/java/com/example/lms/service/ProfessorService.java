package com.example.lms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.AttendanceHistory;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Student;
import com.example.lms.mapper.ProfessorMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfessorService {
	@Autowired
	ProfessorMapper professorMapper;

	// 출석 수정
	public int updateHistory(AttendanceHistory ah) {
		return professorMapper.updateHistory(ah);	
	}
	
	public AttendanceHistory getHistoryByHN(int historyNo) {
		return professorMapper.getHistoryByHN(historyNo);
	}
	
	public int updateAttendanceFromUpdateHistory(AttendanceHistory ah) {
		return professorMapper.updateAttendanceFromUpdateHistory(ah);
	}
	
	// 출석내역목록
	public List<AttendanceHistory> attendanceHistoryList(Map<String, Object> param) {
		return professorMapper.attendanceHistoryList(param);
	}
	
	public int getHistoryCount(Map<String, Object> param) {
		return professorMapper.getHistoryCount(param);
	}
	
	// 출석체크
	public int insertAttendance(Attendance a) {
		return professorMapper.insertAttendance(a);
	}
	
	public int insertHistoryFromAddAttendance(Attendance a) {
		return professorMapper.insertHistoryFromAddAttendance(a);
	}
	
	// 출석체크(학생목록)
	public List<CourseStudent> getStudentListByCourse(int courseNo) {
		return professorMapper.getStudentListByCourse(courseNo);
	}
	
	// 출석체크(강의목록)
	public List<Course> getAttendance(int empNo) {
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		List<Course> courseList = professorMapper.getAttandance(m);
		
		return courseList;
	}
	
	// 학생 리스트
	public List<Student> studentListByPage(int deptNo, int currentPage, String searchWord) {
		int rowPerPage = 10;
		int beginRow = (currentPage - 1) * rowPerPage;
		Map<String, Object> m = new HashMap<>();
		m.put("deptNo", deptNo);
		m.put("rowPerPage", rowPerPage);
		m.put("beginRow", beginRow);
		m.put("searchWord", searchWord);
		List<Student> studentList = professorMapper.studentListByPage(m);
		log.debug("studentList : {}", studentList);
		
		return studentList;
	}
	
	public int getStudentCount(int deptNo, String searchWord) {
		Map<String, Object> m = new HashMap<>();
		m.put("deptNo", deptNo);
		m.put("searchWord", searchWord);
		return professorMapper.getStudentCount(m);
	}
	
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
