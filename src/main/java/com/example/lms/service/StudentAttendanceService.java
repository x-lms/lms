package com.example.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.Course;
import com.example.lms.dto.Student;
import com.example.lms.mapper.StudentAttendanceMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentAttendanceService {

    @Autowired
    private StudentAttendanceMapper attendanceMapper;

    // 학생 정보
    public Student getStudentInfo(int studentNo) {
        return attendanceMapper.getStudentInfo(studentNo);
    }

    // 수강 과목 목록
    public List<Course> getCourseList(int studentNo) {
        return attendanceMapper.getCourseList(studentNo);
    }

    // 출결 상세
    public Map<String, Object> getAttendanceDetail(int studentNo, int courseNo) {
        Map<String, Object> result = new HashMap<>();

        // 출결 요약
        Map<String, Integer> summary = attendanceMapper.getAttendanceSummary(studentNo, courseNo);

        // 날짜별 출결 상세
        List<Attendance> detail = attendanceMapper.getAttendanceDetail(studentNo, courseNo);

        result.put("summary", summary);
        result.put("detail", detail);

        return result;
    }
}
