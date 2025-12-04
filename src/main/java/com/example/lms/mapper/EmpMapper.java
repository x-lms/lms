package com.example.lms.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.*;

@Mapper
public interface EmpMapper {
	// 공지사항 추가
	int insertNotice(Notice n);
	// 공지사항 수정
	int updateNotice(Notice n);
	// 공지사항 삭제
	int deleteNotice(int noticeNo);
	// 파일 추가
	int insertNoticeFile(NoticeFile nf);
	// 공지사항 파일 삭제
	int deleteNoticeFile(NoticeFile nf);
	int deleteNoticeFileByNoticeNo(int noticeNo);
	// 학과 목록
	List<Dept> selectDeptList();
	// 교수 추가
	int insertProfessor(Emp e);
	// 교수 리스트
	List<ProfessorInfo> selectProfessorList(SearchList sl);
	int countProfessor(SearchList sl);
	// 교수 정보
	ProfessorInfo selectProfessor(int prfNo);
	// 교수 정보 수정
	int updateProfessor(ProfessorInfo pi);
	// 교수 정보 삭제
	int deleteProfessor(int prfNo);
	// 학생 리스트
	List<Student> selectStudentList(SearchList sl);
	int countStudent(SearchList sl);
	// 학생 정보
	Student selectStudent(int studentNo);
	// 학생 추가
	void insertStudent(Student s);
	int checkStudentNo(int studentNo);
	int checkDeptNo(int deptNo);
	Integer selectMaxStdNo(String n);
	// 학생 수정
	int updateStudent(Student s);
}
