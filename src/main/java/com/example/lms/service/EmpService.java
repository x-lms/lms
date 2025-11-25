package com.example.lms.service;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.*;
import com.example.lms.mapper.EmpMapper;

@Service
@Transactional
public class EmpService {
	@Autowired
	EmpMapper empMapper;
	public void insertNotice(Notice n, MultipartFile[] files, String path) {
		// 1) 공지 저장
		empMapper.insertNotice(n);
		
		// 2) 파일 배열처리
		if (files != null && files.length > 0) {
			File dir = new File(path);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			
			for(MultipartFile file : files) {
				if (file == null || file.isEmpty()) {
					continue;	// 업로드 안된 파일 스킵
				}
					
				String originName = file.getOriginalFilename();
				String ext = originName.substring(originName.lastIndexOf("."));
				String saveName = UUID.randomUUID().toString().replace("-", "") + ext;
				
				File dest = new File(dir, saveName);

				try {
					file.transferTo(dest);
					
					// NoticeFile DTO 생성 및 DB insert
					NoticeFile nf = new NoticeFile();
					nf.setNoticeNo(n.getNoticeNo());
					nf.setFileName(saveName);	// 저장된 파일명
					nf.setOriginName(originName);	// 원본 파일명
					
					empMapper.insertNoticeFile(nf);
				} catch (Exception e) {
					throw new RuntimeException("파일 저장 실패: " + originName, e);
				}
			}
			
			
		}
	}
	
}

