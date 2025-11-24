package com.example.lms.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Course;
import com.example.lms.dto.Emp;


@Mapper
public interface ProfessorMapper {
	
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
