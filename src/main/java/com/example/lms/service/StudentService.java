package com.example.lms.service;

import com.example.lms.dto.Student;
import com.example.lms.dto.Course;
import com.example.lms.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {

@Autowired
private StudentMapper studentMapper;

// 학생 상세 정보 조회
public Student getStudentDetail(int studentNo) {
    return studentMapper.selectStudentDetail(studentNo);
}

// 학생 정보 수정
public int updateStudent(Student student) {
    return studentMapper.updateStudent(student);
}

// 학생 목록 조회
public List<Student> getStudentList() {
    return studentMapper.selectStudentList();
}

public List<Course> getStudentCourses(int studentNo) {
    return studentMapper.selectCoursesByStudentNo(studentNo);
	}
}
