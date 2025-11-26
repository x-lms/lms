package com.example.lms.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageInfo {
	private int currentPage;   // 현재 페이지
    private int rowPerPage;    // 한 페이지에 표시할 항목 수
    private int pageBlock;     // 한 블록에 표시할 페이지 수
    private int totalCount;    // 전체 데이터 수
    private int lastPage;      // 마지막 페이지
    private int startPage;     // 블록 시작 페이지
    private int endPage;       // 블록 끝 페이지
    private Integer prePage;   // 이전 블록 페이지
    private Integer nextPage;  // 다음 블록 페이지
    private List<Integer> pageList; // 화면에 보여줄 페이지 번호
}
