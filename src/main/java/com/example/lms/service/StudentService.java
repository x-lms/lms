package com.example.lms.service;

import com.example.lms.dto.Assignment;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.StudentCourse;
import com.example.lms.dto.TimetableCell;
import com.example.lms.dto.Student;
import com.example.lms.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;

    // ================= 기존 기능 =================
    public Student getStudentDetail(int studentNo) {
        return studentMapper.selectStudentDetail(studentNo);
    }

    public int updateStudent(Student student) {
        return studentMapper.updateStudent(student);
    }

    public List<Student> getStudentList() {
        return studentMapper.selectStudentList();
    }

    public List<StudentCourse> getStudentCourses(int studentNo) {
        List<com.example.lms.dto.Course> coursesFromDB = studentMapper.selectCoursesByStudentNo(studentNo);
        return mapCoursesToStudentCourses(coursesFromDB);
    }

    // ================= 강의 신청 =================
    @Transactional
    public void applyCourse(int studentNo, int courseNo) throws Exception {
        // 중복 신청 체크
        int duplicate = studentMapper.checkDuplicateCourse(studentNo, courseNo);
        if (duplicate > 0) {
            throw new Exception("이미 신청한 강의입니다.");
        }

        // 개별 강의 조회
        com.example.lms.dto.Course course = studentMapper.selectCourseByCourseNo(courseNo);
        if (course == null) {
            throw new Exception("해당 강의를 찾을 수 없습니다.");
        }

        StudentCourse newCourse = mapCourseToStudentCourse(course);

        // 정원 체크
        if (newCourse.getCurrentCnt() >= newCourse.getMaxCnt()) {
            throw new Exception("강의 정원이 가득 찼습니다.");
        }

        // 기존 신청 강의 시간과 겹치는지 체크
        List<StudentCourse> enrolledCourses = getStudentCourses(studentNo);
        for (StudentCourse enrolled : enrolledCourses) {
            for (CourseTime exist : enrolled.getCourseTimes()) {
                for (CourseTime newTime : newCourse.getCourseTimes()) {
                    if (exist.getCoursedate().equals(newTime.getCoursedate()) &&
                        timeOverlap(exist.getCourseTimeStart(), exist.getCourseTimeEnd(),
                                    newTime.getCourseTimeStart(), newTime.getCourseTimeEnd())) {
                        throw new Exception("강의 시간이 겹쳐서 신청할 수 없습니다.");
                    }
                }
            }
        }

        // 신청 처리 (Map으로 전달)
        Map<String, Integer> params = Map.of("studentNo", studentNo, "courseNo", courseNo);
        studentMapper.insertStudentCourse(params);
    }

    private boolean timeOverlap(String start1, String end1, String start2, String end2) {
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }

    // ================= map 함수 =================
    private StudentCourse mapCourseToStudentCourse(com.example.lms.dto.Course c) {
        StudentCourse sc = new StudentCourse();
        sc.setCourseNo(c.getCourseNo());
        sc.setCourseName(c.getCourseName());
        sc.setCoursePeriod(c.getCoursePeriod());
        sc.setCoursePlan(c.getCoursePlan());
        sc.setCurrentCnt(c.getCurrentCnt());
        sc.setMaxCnt(c.getMaxCnt());
        sc.setFull(c.getCurrentCnt() >= c.getMaxCnt());
        sc.setEmpName(studentMapper.selectEmpNameByCourseNo(c.getCourseNo()));

        List<CourseTime> times = studentMapper.selectCourseTimesByCourseNo(c.getCourseNo());
        sc.setCourseTimes(times);

        if (!times.isEmpty()) {
            CourseTime firstTime = times.get(0);
            sc.setCourseLocation(firstTime.getCourseLocation());
            sc.setCourseDate(firstTime.getCoursedate());
            sc.setCourseTimeStart(firstTime.getCourseTimeStart());
            sc.setCourseTimeEnd(firstTime.getCourseTimeEnd());
        }

        return sc;
    }

    private List<StudentCourse> mapCoursesToStudentCourses(List<com.example.lms.dto.Course> coursesFromDB) {
        List<StudentCourse> studentCourses = new ArrayList<>();
        for (com.example.lms.dto.Course c : coursesFromDB) {
            if (c != null) studentCourses.add(mapCourseToStudentCourse(c));
        }
        return studentCourses;
    }

    // ================= 페이징 + 검색 =================
    public List<StudentCourse> searchAvailableCourses(int studentNo, String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<com.example.lms.dto.Course> coursesFromDB = studentMapper.selectCoursesByKeywordWithPaging(studentNo, keyword, offset, pageSize);

        // 이미 신청한 강의 제외
        Set<Integer> enrolled = studentMapper.selectCoursesByStudentNo(studentNo)
                .stream().map(c -> c.getCourseNo())
                .collect(Collectors.toSet());
        List<com.example.lms.dto.Course> filtered = coursesFromDB.stream()
                .filter(c -> !enrolled.contains(c.getCourseNo()))
                .toList();

        return mapCoursesToStudentCourses(filtered);
    }

    public int countSearchAvailableCourses(int studentNo, String keyword) {
        return studentMapper.countCoursesByKeyword(studentNo, keyword);
    }

    public List<StudentCourse> getAvailableCoursesWithTimes(int studentNo, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<com.example.lms.dto.Course> coursesFromDB = studentMapper.selectAvailableCoursesWithPaging(studentNo, offset, pageSize);
        return mapCoursesToStudentCourses(coursesFromDB);
    }

    public int countAvailableCourses(int studentNo) {
        return studentMapper.countAvailableCourses(studentNo);
    }

 // ========================= 학생 시간표 =========================
    public List<TimetableCell> getStudentSchedule(int studentNo) {

        // 학생이 신청한 모든 강의 조회
        List<StudentCourse> courses = getStudentCourses(studentNo);

        // 시간표 기본 틀 (1~9교시)
        List<TimetableCell> table = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            TimetableCell cell = new TimetableCell();
            cell.setPeriod(i);
            table.add(cell);
        }

        // 과목별 시간 배치
        for (StudentCourse course : courses) {
            for (CourseTime time : course.getCourseTimes()) {
            	String day = switch(time.getCoursedate().toUpperCase()) {
            	    case "월", "MON" -> "MON";
            	    case "화", "TUE" -> "TUE";
            	    case "수", "WED" -> "WED";
            	    case "목", "THU" -> "THU";
            	    case "금", "FRI" -> "FRI";
            	    default -> ""; 
            	};


                // 시작/종료 시간 → 교시
                int startPeriod = convertTimeToPeriod(time.getCourseTimeStart());
                int endPeriod = convertTimeToPeriod(time.getCourseTimeEnd());

                // 범위 내 교시에 과목명 삽입
                for (int p = startPeriod; p <= endPeriod; p++) {
                    if (p < 1 || p > table.size()) continue;

                    TimetableCell row = table.get(p - 1);
                    String existing = getCellValueByDay(row, day);
                    if (!existing.isEmpty()) {
                        // 겹치는 경우 "과목1 / 과목2"
                        setCellValueByDay(row, day, existing + " / " + course.getCourseName());
                    } else {
                        setCellValueByDay(row, day, course.getCourseName());
                    }
                }
            }
        }

        return table;
    }

    // 교시 변환 (HH:mm → 교시 1~9)
    private int convertTimeToPeriod(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        return switch (hour) {
            case 9 -> 1;
            case 10 -> 2;
            case 11 -> 3;
            case 12 -> 4;
            case 13 -> 5;
            case 14 -> 6;
            case 15 -> 7;
            case 16 -> 8;
            case 17 -> 9;
            default -> 0;
        };
    }

    // 요일별 getter
    private String getCellValueByDay(TimetableCell cell, String day) {
        return switch (day) {
            case "MON" -> cell.getMon();
            case "TUE" -> cell.getTue();
            case "WED" -> cell.getWed();
            case "THU" -> cell.getThu();
            case "FRI" -> cell.getFri();
            default -> "";
        };
    }

    // 요일별 setter
    private void setCellValueByDay(TimetableCell cell, String day, String value) {
        switch (day) {
            case "MON" -> cell.setMon(value);
            case "TUE" -> cell.setTue(value);
            case "WED" -> cell.setWed(value);
            case "THU" -> cell.setThu(value);
            case "FRI" -> cell.setFri(value);
        }
    }
 // ========================= 강의 상세보기 =========================

 // 강의 상세 정보 조회
 public StudentCourse getCourseDetail(int studentNo, int courseNo) {

     // 학생이 신청한 전체 강의 조회
     List<StudentCourse> list = getStudentCourses(studentNo);

     // 해당 courseNo에 해당하는 강의 선택
     return list.stream()
             .filter(c -> c.getCourseNo() == courseNo)
             .findFirst()
             .orElse(null);
 }


 // ========================= 과제 목록 =========================
 public List<Assignment> getAssignments(int courseNo) {
     return studentMapper.selectAssignmentsByCourseNo(courseNo);
 }


 // ========================= 수강 취소 가능 여부 =========================
 public boolean isCancelable(int studentNo, int courseNo) {
	    Map<String, Object> info = studentMapper.selectCourseEnrollInfo(studentNo, courseNo);
	    if (info == null) return false;

	    String status = (String) info.get("status");
	    if (!"신청".equals(status)) return false;

	    // coursePeriod 가져오기
	    String coursePeriod = (String) info.get("coursePeriod"); // ex: "2025-03-01~2025-06-30"
	    if (coursePeriod == null || !coursePeriod.contains("~")) return false;

	    String[] dates = coursePeriod.split("~");
	    java.time.LocalDate start = java.time.LocalDate.parse(dates[0].trim());
	    java.time.LocalDate end = java.time.LocalDate.parse(dates[1].trim());
	    java.time.LocalDate today = java.time.LocalDate.now();

	    // 수업 시작일 이후면 취소 불가
	    if (!today.isBefore(start)) {
	        return false;
	    }

	    return true;
	}

 @Transactional
 public void cancelCourse(int studentNo, int courseNo) throws Exception {
     // 수업 시작일 체크
     if (!isCancelable(studentNo, courseNo)) {
         throw new Exception("이미 수업이 시작되어 취소할 수 없습니다.");
     }

     // registration 테이블에서 삭제
     Map<String, Integer> params = Map.of("studentNo", studentNo, "courseNo", courseNo);
     studentMapper.deleteStudentCourse(params);
 }

 
}
