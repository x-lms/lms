package com.example.lms.service;

import com.example.lms.dto.Student;
import com.example.lms.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {

@Autowired
private StudentMapper studentMapper;

public Student getStudentDetail(int studentNo) {
    return studentMapper.selectStudentDetail(studentNo);
}

public int updateStudent(Student student) {
    return studentMapper.updateStudent(student);
}

public List<Student> getStudentList() {
    return studentMapper.selectStudentList();
}


}
