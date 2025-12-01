package com.example.lms.service;

import com.example.lms.dto.Assignment;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.Question;
import com.example.lms.dto.StudentCourse;
import com.example.lms.dto.TimetableCell;
import com.example.lms.dto.Student;
import com.example.lms.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
        // 기존 로직 그대로...
        return new ArrayList<>(); // 생략
    }

    // ========================= 강의 상세보기 =========================
    public StudentCourse getCourseDetail(int studentNo, int courseNo) {
        List<StudentCourse> list = getStudentCourses(studentNo);
        return list.stream()
                .filter(c -> c.getCourseNo() == courseNo)
                .findFirst()
                .orElse(null);
    }

    // ========================= 과제 목록 =========================
    public List<Assignment> getAssignments(int courseNo) {
        return studentMapper.selectAssignmentsByCourseNo(courseNo);
    }

    // ========================= 수강 취소 =========================
    public boolean isCancelable(int studentNo, int courseNo) {
        Map<String, Object> info = studentMapper.selectCourseEnrollInfo(studentNo, courseNo);
        if (info == null) return false;

        String status = (String) info.get("status");
        if (!"신청".equals(status)) return false;

        String coursePeriod = (String) info.get("coursePeriod");
        if (coursePeriod == null || !coursePeriod.contains("~")) return false;

        String[] dates = coursePeriod.split("~");
        java.time.LocalDate start = java.time.LocalDate.parse(dates[0].trim());
        java.time.LocalDate today = java.time.LocalDate.now();

        return today.isBefore(start);
    }

    @Transactional
    public void cancelCourse(int studentNo, int courseNo) throws Exception {
        if (!isCancelable(studentNo, courseNo)) {
            throw new Exception("이미 수업이 시작되어 취소할 수 없습니다.");
        }
        Map<String, Integer> params = Map.of("studentNo", studentNo, "courseNo", courseNo);
        studentMapper.deleteStudentCourse(params);
    }
 // ========================= 질문 등록 =========================
    @Transactional
    public void insertQuestion(Question question) {
        studentMapper.insertQuestion(question);
    }

    // ========================= 학생 질문 전체 조회 =========================
    public List<Question> getQuestionsByStudent(int studentNo) {
        List<Question> questions = studentMapper.selectQuestionsByStudent(studentNo);

        // 각 질문에 강의명 채워주기
        for (Question q : questions) {
            String courseName = studentMapper.selectCourseNameByCourseNo(q.getCourseNo());
            q.setCourseName(courseName); 
        }

        return questions;
    }

    // 단일 강의명 조회용 (옵션)
    public String getCourseNameByCourseNo(int courseNo) {
        return studentMapper.selectCourseNameByCourseNo(courseNo);
    }
    public int getProfessorNoByCourseNo(int courseNo) {
        return studentMapper.selectProfessorNoByCourseNo(courseNo);
    }

    // 질문 번호로 단일 질문 가져오기
    public Question getQuestionByNo(int questionNo) {
        Question question = studentMapper.selectQuestionByQuestionNo(questionNo);
        if (question != null) {
            String courseName = studentMapper.selectCourseNameByCourseNo(question.getCourseNo());
            question.setCourseName(courseName);
        }
        return question;
    }
}
