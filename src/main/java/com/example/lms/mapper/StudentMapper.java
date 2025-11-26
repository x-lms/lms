package com.example.lms.mapper;

import com.example.lms.dto.Course;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {

    // ================= 학생/강의 관련 =================
    Student selectStudentDetail(int studentNo);
    int updateStudent(Student student);
    List<Student> selectStudentList();

    List<Course> selectCoursesByStudentNo(int studentNo);
    List<Course> selectAvailableCourses(int studentNo);

    int checkDuplicateCourse(@Param("studentNo") int studentNo, @Param("courseNo") int courseNo);
    void insertStudentCourse(@Param("studentNo") int studentNo, @Param("courseNo") int courseNo);
    void insertStudentCourse(Map<String, Integer> params); // applyCourse용

    String selectEmpNameByCourseNo(int courseNo);

    List<CourseTime> selectCourseTimesByCourseNo(int courseNo);

    // ================= 개별 강의 조회 =================
    Course selectCourseByCourseNo(int courseNo);

    // ================= 페이징 + 검색 =================
    List<Course> selectCoursesByKeywordWithPaging(
            @Param("studentNo") int studentNo,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    int countCoursesByKeyword(
            @Param("studentNo") int studentNo,
            @Param("keyword") String keyword
    );

    List<Course> selectAvailableCoursesWithPaging(
            @Param("studentNo") int studentNo,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    int countAvailableCourses(int studentNo);
}
