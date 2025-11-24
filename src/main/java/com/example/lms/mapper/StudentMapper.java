package com.example.lms.mapper;

import com.example.lms.dto.Student;
import com.example.lms.dto.Course;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StudentMapper {

// 학생 상세 조회
Student selectStudentDetail(int studentNo);

// 학생 정보 수정
int updateStudent(Student student);

// 학생 목록 조회 (선택 사항)
List<Student> selectStudentList();

// 학생 수강과목 조회
List<Course> selectCoursesByStudentNo(int studentNo);


}
