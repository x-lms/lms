package com.example.lms.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Dept;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Notice;
import com.example.lms.dto.NoticeFile;
import com.example.lms.dto.ProfessorInfo;
import com.example.lms.dto.SearchList;

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
}
