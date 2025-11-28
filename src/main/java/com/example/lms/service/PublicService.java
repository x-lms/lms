package com.example.lms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lms.dto.*;
import com.example.lms.mapper.PublicMapper;

@Service
@Transactional
public class PublicService {
	@Autowired
	PublicMapper publicMapper;
	
	public Student loginStudent(LoginUser lu) {
		return publicMapper.selectLoginStudent(lu);
	}
	public Emp loginProfessor(LoginUser lu) {
		return publicMapper.selectLoginProfessor(lu);
	}
	public Emp loginEmp(LoginUser lu) {
		return publicMapper.selectLoginEmp(lu);
	}
	
	// 공지사항 리스트
	private static final int ROW_PER_PAGE = 10;	// 한 페이지에 보여질 글 수
	public List<Notice> getNoticeList(int currentPage, String searchWord) {
		int beginRow = (currentPage - 1) * ROW_PER_PAGE;
		SearchList sl = new SearchList();
		sl.setRowPerPage(ROW_PER_PAGE);
		sl.setBeginRow(beginRow);
		sl.setSearchWord(searchWord);
		return publicMapper.selectNoticeList(sl);
	}
	public int countNotice(String searchWord) {
		return publicMapper.countNotice(searchWord);
	}
	public int getRowPerPage() {
		return ROW_PER_PAGE;
	}
	
	// 공지사항 상세페이지
	public Notice getNoticeOne(int nno) {
		return publicMapper.selectNoticeOne(nno);
	}
	public List<NoticeFile> getNoticeFileList(int noticeNo) {
		return publicMapper.selectNoticeFile(noticeNo);
	}
	public NoticeFile getNoticeFile(int fileNo) {
		return publicMapper.selectNoticeFileOne(fileNo);
	}
}
