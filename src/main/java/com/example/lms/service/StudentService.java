package com.example.lms.service;

import com.example.lms.dto.CourseTime;
import com.example.lms.dto.StudentCourse;
import com.example.lms.dto.Student;
import com.example.lms.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
