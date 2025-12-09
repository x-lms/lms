package com.example.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.*;
@Mapper
public interface PublicMapper {
	// 로그인
	Student selectLoginStudent(LoginUser loginUser);
	Emp selectLoginProfessor(LoginUser loginUser);
	Emp selectLoginEmp(LoginUser loginUser);
	
	// 아이디 비밀번호 찾기
	String findStudentId(Student std);
	String findEmpId(Emp e);
	int findStudentPw(Student std);
	int findEmpPw(Emp e);
	int changeStudentPw(Student std);
	int changeEmpPw(Emp e);
	
	// 공지사항 리스트
	List<Notice> selectNoticeList(SearchList sl);
	int countNotice(String searchWord);
	
	// 공지사항 상세페이지
	Notice selectNoticeOne(int noticeNo);
	List<NoticeFile> selectNoticeFile(int noticeNo);
	NoticeFile selectNoticeFileOne(int fileNo);
	
	// 스케줄 상세
	Schedule selectSchduleOne(int scheduleNo);
	Emp selectEmp(int empNo);
	// 스케줄 리스트
	List<Schedule> selectSchduleList();
}
