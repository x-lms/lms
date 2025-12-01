package com.example.lms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.AttendanceHistory;
import com.example.lms.dto.AttendanceSummary;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Project;
import com.example.lms.dto.ProjectResult;
import com.example.lms.dto.Student;
import com.example.lms.dto.TimetableCell;
import com.example.lms.mapper.ProfessorMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class ProfessorService {
	@Autowired
	ProfessorMapper professorMapper;
	
	// 과제 점수 등록
	public int addResultScore(ProjectResult pr) {
		return professorMapper.addResultScore(pr);
	}
	
	// 과게 결과물 상세보기
	public ProjectResult projectResultOne(int resultNo) {
		return professorMapper.projectResultOne(resultNo);
	}

	// 과제 결과물 목록
	public List<ProjectResult> projectResultList(int projectNo) {
		return professorMapper.projectResultList(projectNo);
	}
	
	// 과제 삭제
	@Transactional
	public boolean deleteProjectIfNoResults(int projectNo) {
		int count = professorMapper.countProjectResults(projectNo);
		if(count > 0 ) {
			// 결과물 있으면 삭제 X
			return false;
		}
		
		// 결과물 없으면 삭제 O
		professorMapper.deleteProjectIfNoResults(projectNo);
		return true;
	}
	
	// 과제 등록
	public int addProject(Project p) {
		return professorMapper.addProject(p);	
	}
	
	// 과제 목록
	public List<Project> projectListByPage(int empNo, int currentPage) {
		int rowPerPage = 10;
		int beginRow = (currentPage - 1) * rowPerPage;
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		m.put("rowPerPage", rowPerPage);
		m.put("beginRow", beginRow);
		List<Project> projectList = professorMapper.projectListByPage(m);
		
		return projectList;
	}
	
	public int getProjectCount(int empNo) {
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		return professorMapper.getProjectCount(m);
	}

	
	// 출석 수정
	public int updateHistory(AttendanceHistory ah) {
		return professorMapper.updateHistory(ah);	
	}
	
	public AttendanceHistory getHistoryByHN(int historyNo) {
		return professorMapper.getHistoryByHN(historyNo);
	}
	
	public int updateAttendanceFromUpdateHistory(AttendanceHistory ah) {
		return professorMapper.updateAttendanceFromUpdateHistory(ah);
	}
	
	// 출석내역목록
	public List<AttendanceHistory> attendanceHistoryList(Map<String, Object> param) {
		return professorMapper.attendanceHistoryList(param);
	}
	
	public int getHistoryCount(Map<String, Object> param) {
		return professorMapper.getHistoryCount(param);
	}
	
	// 출석체크
	public int insertAttendance(Attendance a) {
		return professorMapper.insertAttendance(a);
	}
	
	public int insertHistoryFromAddAttendance(Attendance a) {
		return professorMapper.insertHistoryFromAddAttendance(a);
	}
	
	// 출석체크(학생목록) 출결상태
	public List<CourseStudent> getStudentListByCourse(int courseNo) {
		List<CourseStudent> studentList = professorMapper.getStudentListByCourse(courseNo);
        List<AttendanceSummary> summaryList = professorMapper.getAttendanceSummaryByCourse(courseNo);

        // Map으로 변환
        Map<Integer, AttendanceSummary> summaryMap = new HashMap<>();
        for (AttendanceSummary s : summaryList) {
            // 지각 3회 -> 결석 1회
            s.setAbsent(s.getAbsent() + s.getLate() / 3);
            s.setTotal(s.getTotal() - s.getAbsent());
            summaryMap.put(s.getStudentNo(), s);
        }

        // 학생 DTO에 AttendanceSummary 연결
        for (CourseStudent cs : studentList) {
            cs.setAttendanceSummary(summaryMap.get(cs.getStudentNo()));
        }

        return studentList;
	}
		
	// 출석체크(강의목록)
	public List<Course> getCourseAttandance(int empNo) {
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		List<Course> courseList = professorMapper.getCourseAttandance(m);
		
		return courseList;
	}
	
	// 학생 리스트
	public List<Student> studentListByPage(int deptNo, int currentPage, String searchWord) {
		int rowPerPage = 10;
		int beginRow = (currentPage - 1) * rowPerPage;
		Map<String, Object> m = new HashMap<>();
		m.put("deptNo", deptNo);
		m.put("rowPerPage", rowPerPage);
		m.put("beginRow", beginRow);
		m.put("searchWord", searchWord);
		List<Student> studentList = professorMapper.studentListByPage(m);
		log.debug("studentList : {}", studentList);
		
		return studentList;
	}
	
	public int getStudentCount(int deptNo, String searchWord) {
		Map<String, Object> m = new HashMap<>();
		m.put("deptNo", deptNo);
		m.put("searchWord", searchWord);
		return professorMapper.getStudentCount(m);
	}
	
	// 강의 상세보기
	public Course getCourseOne(int courseNo) {
		return professorMapper.courseOne(courseNo);
	}
	public List<CourseTime> getCourseTimeList(int courseNo) {
		return professorMapper.getCourseTimeList(courseNo);
	}
	
	// 강의 등록
	public int insertCourse(Course c) {
		return professorMapper.insertCourse(c);		
	}
	public int insertCourseTime(CourseTime ct) {
		return professorMapper.insertCourseTime(ct);		
	}
	
	// 강의 수정
	public int updateCourse(Course c) {
		return professorMapper.updateCourse(c);		
	}
	
	public CourseTime getCourseTime(int courseNo) {
		return professorMapper.getCourseTime(courseNo);
	}

	public int updateCourseTime(CourseTime ct) {
		return professorMapper.updateCourseTime(ct);			
	}
	
	// 강의 삭제
	public int deleteCourse(int courseNo) {
		return professorMapper.deleteCourse(courseNo);		
	}
	public int deleteCourseTime(int courseNo) {
		return professorMapper.deleteCourseTime(courseNo);		
	}
		
	// 강의리스트
	public List<Course> courseListByPage(int empNo, int currentPage, String searchWord) {
		int rowPerPage = 10;
		int beginRow = (currentPage - 1) * rowPerPage;
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		m.put("rowPerPage", rowPerPage);
		m.put("beginRow", beginRow);
		m.put("searchWord", searchWord);
		List<Course> courseList = professorMapper.courseListByPage(m);
		log.debug("courseList : {}", courseList);
		
		return courseList;
	}
	
	public int getCourseCount(int empNo, String searchWord) {
		Map<String, Object> m = new HashMap<>();
		m.put("empNo", empNo);
		m.put("searchWord", searchWord);
		return professorMapper.getCourseCount(m);
	}
	
	
	// 내 정보 수정 
	public int updateProfessorInfo(Emp e) {
		return professorMapper.updateProfessorInfo(e);
	}
	
	// 내 정보
	public Emp professorInfo(int empNo) {
		return professorMapper.professorInfo(empNo);
	}
	
	// 홈 화면
	public List<TimetableCell> getFullTimetable(int empNo) {
		List<CourseTime> courseTimes  = professorMapper.selectAllCourseTimes(empNo);
		
		// 1~9교시 기본 셀 생성
	    List<TimetableCell> timetable = new ArrayList<>();
	    for (int i = 1; i <= 9; i++) {
	        TimetableCell cell = new TimetableCell();
	        cell.setPeriod(i);
	        timetable.add(cell);
	    }

	    // 각 CourseTime을 맞는 교시에 채우기
	    for (CourseTime ct : courseTimes) {
	        int period = getPeriodFromTime(ct.getCourseTimeStart());
	        if (period <= 0 || period > 9) continue;

	        TimetableCell cell = timetable.get(period - 1); // 0-based
	        switch (ct.getCoursedate()) {
	            case "월": cell.setMon(ct.getCourseName()); break;
	            case "화": cell.setTue(ct.getCourseName()); break;
	            case "수": cell.setWed(ct.getCourseName()); break;
	            case "목": cell.setThu(ct.getCourseName()); break;
	            case "금": cell.setFri(ct.getCourseName()); break;
	        }
	    }
		
		return timetable;
	}

	 // 시간별 교시 매핑
    private int getPeriodFromTime(String startTime) {
        // startTime 형식: "09:00"
        int hour = Integer.parseInt(startTime.split(":")[0]);
        switch (hour) {
            case 9: return 1;
            case 10: return 2;
            case 11: return 3;
            case 12: return 4;
            case 13: return 5;
            case 14: return 6;
            case 15: return 7;
            case 16: return 8;
            case 17: return 9;
            default: return 0; // 범위 밖
        }
    }
    
    // 캘린더
	public List<Map<String, String>> getProfessorSchedule() {		
		return professorMapper.getProfessorSchedule();
	}

	

	

	

}
