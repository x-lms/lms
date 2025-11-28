package com.example.lms.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Notice;
import com.example.lms.dto.NoticeFile;

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
}
