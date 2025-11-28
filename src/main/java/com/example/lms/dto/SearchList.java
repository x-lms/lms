package com.example.lms.dto;

import lombok.Data;

@Data
public class SearchList {
	private int rowPerPage;
	private int beginRow;
	private String searchCategory;
	private String searchWord;
}
