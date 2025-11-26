package com.example.lms.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Student;


@Mapper
public interface ProfessorMapper {
	
	// 출석체크
	int insertAttendance(Attendance a);
	
	// 출석체크 (학생리스트)
	List<CourseStudent> getStudentListByCourse(int courseNo);
	
	// 출석체크 (강의목록)
	List<Course> getAttandance(Map<String, Object> m);
	
	// 학생리스트
	List<Student> studentListByPage(Map<String, Object> m);
	int getStudentCount(Map<String, Object> m);
	
	// 강의 상세보기
	Course courseOne(int courseNo);
	// 강의등록
	int insertCourse(Course c);
	// 강의수정
	int updateCourse(Course c);
	// 강의삭제
	int deleteCourse(int courseNo);
	
	// 강의리스트
	List<Course> courseListByPage(Map<String, Object> m);
	int getCourseCount(Map<String, Object> m);
	
	// 내 정보
	Emp professorInfo(int empNo);
	// 내 정보 수정
	int updateProfessorInfo(Emp e);




}
