package com.example.lms.mapper;

import com.example.lms.dto.Assignment;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.Project;
import com.example.lms.dto.ProjectResult;
import com.example.lms.dto.Student;
import com.example.lms.dto.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {

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

    Course selectCourseByCourseNo(int courseNo);

    List<Course> selectCoursesByKeywordWithPaging(
            @Param("studentNo") int studentNo,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    int countCoursesByKeyword(@Param("studentNo") int studentNo, @Param("keyword") String keyword);

    List<Course> selectAvailableCoursesWithPaging(
            @Param("studentNo") int studentNo,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    int countAvailableCourses(int studentNo);
    List<Assignment> selectAssignmentsByCourseNo(int courseNo);
    Map<String, Object> selectCourseEnrollInfo(int studentNo, int courseNo);

    int deleteStudentCourse(Map<String, Integer> params);

    // ========================= 질문 관련 =========================
    void insertQuestion(Question question);
    void updateQuestion(Question question);
    void deleteQuestion(@Param("questionNo") int questionNo);
    List<Question> selectQuestionsByStudent(@Param("studentNo") int studentNo);
    Question selectQuestionByQuestionNo(@Param("questionNo") int questionNo);
    String selectCourseNameByCourseNo(@Param("courseNo") int courseNo);

    // ========================= 교수 번호 조회 (수정) =========================
 
    Integer selectProfessorNoByCourseNo(@Param("courseNo") int courseNo);
    
    //============================== 과제 전송 =================================
    
    // 프로젝트 단일 조회
    Project selectProjectByProjectNo(@Param("projectNo") int projectNo);
    // 학생 제출 여부 조회
    ProjectResult selectProjectResultByProjectNoAndStudentNo(
        @Param("projectNo") int projectNo,
        @Param("studentNo") int studentNo
    );
    // 과제 제출 insert
    void insertProjectResult(ProjectResult result);
    // projectNo로 강의번호 조회
    int selectCourseNoByProjectNo(@Param("projectNo") int projectNo);

    int updateProjectResult(ProjectResult result);
	ProjectResult getProjectResultByResultNo(int resultNo);

}
