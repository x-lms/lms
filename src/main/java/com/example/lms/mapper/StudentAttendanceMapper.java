package com.example.lms.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.Course;
import com.example.lms.dto.Student;

@Mapper
public interface StudentAttendanceMapper {

    Student getStudentInfo(@Param("studentNo") int studentNo);
    List<Course> getCourseList(int studentNo);

    Map<String, Integer> getAttendanceSummary(
        @Param("studentNo") int studentNo,
        @Param("courseNo") int courseNo
    );

    List<Attendance> getAttendanceDetail(
        @Param("studentNo") int studentNo,
        @Param("courseNo") int courseNo
    );
}

