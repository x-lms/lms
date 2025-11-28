package com.example.lms.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.AttendanceHistory;
import com.example.lms.dto.AttendanceSummary;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Project;
import com.example.lms.dto.Student;
import com.example.lms.dto.TimetableCell;


@Mapper
public interface ProfessorMapper {
	
	// 과제 목록
	List<Project> projectListByPage(Map<String, Object> m);
	int getProjectCount(Map<String, Object> m);
	
	// 출석 수정
	int updateHistory(AttendanceHistory ah);
	int updateAttendanceFromUpdateHistory(AttendanceHistory ah);
	AttendanceHistory getHistoryByHN(int historyNo);
	
	// 출석내역목록
	List<AttendanceHistory> attendanceHistoryList(Map<String, Object> param);
	int getHistoryCount(Map<String, Object> param);
	
	// 출석체크
	int insertAttendance(Attendance a);
	int insertHistoryFromAddAttendance(Attendance a);
	
	// 출석체크 (학생리스트)
	List<CourseStudent> getStudentListByCourse(int courseNo);
	// 총 출결상태
	List<AttendanceSummary> getAttendanceSummaryByCourse(int courseNo);
	
	// 출석체크 (강의목록)
	List<Course> getAttandance(Map<String, Object> m);
	
	// 학생리스트
	List<Student> studentListByPage(Map<String, Object> m);
	int getStudentCount(Map<String, Object> m);
	
	// 강의 상세보기
	Course courseOne(int courseNo);
	List<CourseTime> getCourseTimeList(int courseNo);
	// 강의등록
	int insertCourse(Course c);
	int insertCourseTime(CourseTime ct);
	// 강의수정
	int updateCourse(Course c);
	CourseTime getCourseTime(int courseNo);
	int updateCourseTime(CourseTime ct);
	// 강의삭제
	int deleteCourse(int courseNo);
	int deleteCourseTime(int courseNo);
	
	// 강의리스트
	List<Course> courseListByPage(Map<String, Object> m);
	int getCourseCount(Map<String, Object> m);
	
	// 내 정보
	Emp professorInfo(int empNo);
	// 내 정보 수정
	int updateProfessorInfo(Emp e);
	
	// 홈 화면
	List<CourseTime> selectAllCourseTimes(int empNo);
	List<Map<String, String>> getProfessorSchedule();
	
	
	
	

	

	
}
