package com.example.lms.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Notice;
import com.example.lms.dto.NoticeFile;

@Mapper
public interface EmpMapper {
	// 공지사항 추가
	int insertNotice(Notice n);
	int insertNoticeFile(NoticeFile nf);
}
