package com.example.lms.mapper;

import com.example.lms.dto.Student;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StudentMapper {
// 학생 정보 상세보기
Student selectStudentDetail(int studentNo);

// 학생 정보 수정
int updateStudent(Student student);

// 학생 리스트 조회 (선택 사항)
List<Student> selectStudentList();

}
