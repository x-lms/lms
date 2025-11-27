package com.example.lms.service;

import java.io.File;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.*;
import com.example.lms.mapper.EmpMapper;
import com.example.lms.mapper.PublicMapper;

@Service
@Transactional
public class EmpService {
	@Autowired EmpMapper empMapper;
	@Autowired PublicMapper publicMapper;
	
	
	public void insertNotice(Notice n, MultipartFile[] files, String path) {
		// 1) 공지 저장
		empMapper.insertNotice(n);
		
		// 2) 파일 저장
		saveFiles(n.getNoticeNo(), files, path);
	}
	
	// 공지사항 수정
	public void modifyNotice(Notice notice, MultipartFile[] files, String deletefiles, String path) {
		// 1) 공지사항 내용 수정
		empMapper.updateNotice(notice);
		
		// 2) 삭제 요청된 파일 삭제 처리
		if(deletefiles != null && !deletefiles.isEmpty()) {
			String[] arr = deletefiles.split(",");
			
			for(String fileNoStr : arr) {
				int fileNo = Integer.parseInt(fileNoStr);
				
				NoticeFile nf = publicMapper.selectNoticeFileOne(fileNo);
				
				if(nf != null) {
					File f = new File(path, nf.getFileName());
					if(f.exists()) f.delete();
					
					empMapper.deleteNoticeFile(nf);
				}
			}
		}
		
		// 3) 새파일 업로드 처리
		saveFiles(notice.getNoticeNo(), files, path);
	}
	
	// 파일 저장 메서드
	private void saveFiles(int noticeNo, MultipartFile[] files, String path) {
		if(files == null || files.length == 0) return;
		
		File dir = new File(path);
		if(!dir.exists()) dir.mkdirs();
		
		for(MultipartFile file : files) {
			if (file == null || file.isEmpty()) continue;
				
			String originName = file.getOriginalFilename();
			String ext = originName.substring(originName.lastIndexOf("."));
			String saveName = UUID.randomUUID().toString().replace("-", "") + ext;
			
			File dest = new File(dir, saveName);

			try {
				file.transferTo(dest);
				
				// NoticeFile DTO 생성 및 DB insert
				NoticeFile nf = new NoticeFile();
				nf.setNoticeNo(noticeNo);
				nf.setFileName(saveName);	// 저장된 파일명
				nf.setOriginName(originName);	// 원본 파일명
				nf.setFileSize(file.getSize());	// 파일 사이즈
				nf.setFileType(file.getContentType());	// 타입 저장
				
				empMapper.insertNoticeFile(nf);
			} catch (Exception e) {
				throw new RuntimeException("파일 저장 실패: " + originName, e);
			}
		}
	}
}

