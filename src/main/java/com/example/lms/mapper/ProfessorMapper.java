package com.example.lms.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.AttendanceHistory;
import com.example.lms.dto.AttendanceSummary;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.CourseWithTime;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Project;
import com.example.lms.dto.ProjectAverage;
import com.example.lms.dto.ProjectResult;
import com.example.lms.dto.Question;
import com.example.lms.dto.Score;
import com.example.lms.dto.Student;
import com.example.lms.dto.StudentScorePF;
import com.example.lms.dto.TimetableCell;


@Mapper
public interface ProfessorMapper {
	
	// 최종성적
	int modifyFinalScore(Score score);
		
	// 성적 등록
   	int addScore(Score score);
	List<StudentScorePF> getStudentListAndScore(int courseNo); // 성적등록 학생목록
	ProjectAverage getAvgProjectScoreByStudent(int courseNo, int studentNo);//(과제점수)
	void updateScoreGrade(Score s); // 성적 grade 계산
	
	// 성적 목록
	List<Score> getScoreByCourse(int courseNo);
	
	// 답글 등록
	int updateAnswer(Question question);
	
	// 문의사항 목록
	List<Question> questionListByPage(Map<String, Object> m);
	Integer getQuestionCount(Map<String, Object> m);

	// 과제 점수 등록
	int addResultScore(ProjectResult pr);
	
	// 결과물 상세보기
	ProjectResult projectResultOne(int resultNo);
	
	// 과제 결과물 목록
	List<ProjectResult> projectResultList(int projectNo);
	
	// 과제 삭제
	int deleteProjectIfNoResults(int projectNo);
	int countProjectResults(int projectNo);

	// 과제 등록
	int addProject(Project p);
	
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
	
	// 출석 체크(하루제한)
	int countAttendanceToday(int studentNo, int courseNo,LocalDate today);
	
	// 출석체크 (학생리스트)
	List<CourseStudent> getStudentListByCourse(int courseNo);
	// 총 출결상태
	List<AttendanceSummary> getAttendanceSummaryByCourse(int courseNo);
	
	// 출석체크 (강의목록)
	List<Course> getCourseAttandanceAndScore(Map<String, Object> m);
	
	// 학생리스트
	List<Student> studentListByPage(Map<String, Object> m);
	int getStudentCount(Map<String, Object> m);
	
	// 강의 상세보기
	Course courseOne(int courseNo);
	List<CourseTime> getCourseTimeList(int courseNo);
	// 강의등록
	int insertCourse(Course c);
	int insertCourseTime(CourseWithTime cwt);
	List<CourseTime> getCourseTimesByEmp(int empNo); // 강의시간
	// 강의수정
	int updateCourse(Course c);
	List<CourseTime> getCourseTime(int courseNo);
	int addCourseTime(CourseTime ct);
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
