package com.example.lms.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.Dept;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Notice;
import com.example.lms.dto.NoticeFile;
import com.example.lms.dto.ProfessorInfo;
import com.example.lms.dto.SearchList;
import com.example.lms.dto.Student;
import com.example.lms.dto.StudentExcelResult;
import com.example.lms.mapper.EmpMapper;
import com.example.lms.mapper.PublicMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	
	// 공지사항 삭제
	public void deleteNotice(int noticeNo, String uploadPath) {
		// 1) notice에 연결된 파일 목록 조회
		List<NoticeFile> files = publicMapper.selectNoticeFile(noticeNo);
		
		// 2) 물리파일 삭제 시도 (실패해도 로그만 남기고 진행)
		if (files != null && !files.isEmpty()) {
			for(NoticeFile nf : files) {
				try {
					if(nf.getFileName() != null) {
						File f = new File(uploadPath, nf.getFileName());
						if(f.exists()) {
							boolean deleted = f.delete();
							if(!deleted) {
								// 삭제 실패 : 로그로 남김
								log.debug("파일삭제 실패: " + f.getAbsolutePath());
							}
						}
					}
				} catch(Exception e) {
					log.debug("파일 삭제 주 예외: " + e.getMessage());
				}
			}
		}
		
		// 3) notice_file 삭제
		empMapper.deleteNoticeFileByNoticeNo(noticeNo);
		
		// 4) 공지 테이블에서 삭제
		empMapper.deleteNotice(noticeNo);
	}
	
	// 학과 목록 불러오기
	public List<Dept> getDeptList() {
		return empMapper.selectDeptList();
	}
	
	// 교수 추가
	public void addProfessor(Emp emp) {
		emp.setEmpRole(2);
		empMapper.insertProfessor(emp);
	}
	
	// 교수 목록
	private static final int ROW_PER_PAGE = 15;	// 한 페이지에 교수 수
	public List<ProfessorInfo> getProfessorList(int currentPage, String searchName, String searchDept) {
		int beginRow = (currentPage - 1) * ROW_PER_PAGE;
		SearchList sl = new SearchList();
		sl.setBeginRow(beginRow);
		sl.setRowPerPage(ROW_PER_PAGE);
		sl.setSearchWord(searchName);
		sl.setSearchCategory(searchDept);
		
		return empMapper.selectProfessorList(sl);
	}
	public int countPrf(String searchName, String searchDept) {
		SearchList sl = new SearchList();
		sl.setSearchWord(searchName);
		sl.setSearchCategory(searchDept);
		return empMapper.countProfessor(sl);
	}
	public int getRowPerPage() {
		return ROW_PER_PAGE;
	}
	
	// 교수 정보
	public ProfessorInfo getProfessorInfo(int prfNo) {
		return empMapper.selectProfessor(prfNo);
	}
	
	// 교수 정보 수정
	public void updateProfessor(ProfessorInfo pi) {
		empMapper.updateProfessor(pi);
	}
	
	// 교수 삭제
	public void deleteProfessor(int prfNo) {
		empMapper.deleteProfessor(prfNo);
	}
	
	// 학생 리스트
	public List<Student> getStdList(int currentPage, String searchName, String searchDept) {
		int beginRow = (currentPage - 1) * ROW_PER_PAGE;
		SearchList sl = new SearchList();
		sl.setBeginRow(beginRow);
		sl.setRowPerPage(ROW_PER_PAGE);
		sl.setSearchWord(searchName);
		sl.setSearchCategory(searchDept);
		
		return empMapper.selectStudentList(sl);
	}
	public int countStd(String searchName, String searchDept) {
		SearchList sl = new SearchList();
		sl.setSearchWord(searchName);
		sl.setSearchCategory(searchDept);
		return empMapper.countStudent(sl);
	}
	// 학생 상세
	
	// 학생 추가(파일)
	public StudentExcelResult readExcel(MultipartFile file) throws Exception {
		StudentExcelResult result = new StudentExcelResult();
		
		try(InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
			Sheet sheet = workbook.getSheetAt(0);
			int lastRow = sheet.getLastRowNum();
			
			result.setTotalCount(lastRow);	// 첫 행 제외
			
			for(int r = 1; r <= lastRow; r++) {	// 0번 행은 헤더
				Row row = sheet.getRow(r);
				if (row == null) continue;
				
				String studentNo = getCellValue(row.getCell(0)); // 학번
				String deptNo = getCellValue(row.getCell(1)); // 학과번호
				String studentName = getCellValue(row.getCell(2)); // 학생이름
				String studentPw = getCellValue(row.getCell(3)); // 비밀번호
				String studentState = getCellValue(row.getCell(4)); // 상태
				
				// 실패 항목 생성 메서드 호출
				StudentExcelResult.FailItem fail = validateRow(r, studentNo, deptNo, studentName, studentPw, studentState);
				if(fail != null) {
					result.getFailList().add(fail);
					continue;
				}
				
				// 성공항목 DTO
				Student std = new Student();
				std.setStudentNo(Integer.parseInt(studentNo));
				std.setDeptNo(Integer.parseInt(deptNo));
				std.setStudentName(studentName);
				std.setStudentPw(studentPw);
				std.setStudentState(studentState);
				
				result.getSuccessList().add(std);
			}
		}
		// 성공/실패 개수 계산
		result.setSuccessCount(result.getSuccessList().size());
		result.setFailCount(result.getFailList().size());
		return result;
	}
	
	// DB 저장
	public int commitExcel(List<Student> list) {
		int count = 0;
		for(Student std : list) {
			empMapper.insertStudent(std);
			count++;
		}
		return count;
	}
	
	// 엑셀 행 데이터 검증
	private StudentExcelResult.FailItem validateRow(int row, String no, String dno, String name, String pw, String state) {
		if(no == null || dno == null || name == null | pw == null || state == null) {
			return createFail(row, no, name, "필수 항목 누락");
		}
		if(empMapper.checkStudentNo(Integer.parseInt(no)) > 0) {
			return createFail(row, no, name, "중복된 학번");
		}
		if(empMapper.checkDeptNo(Integer.parseInt(dno)) == 0) {
			return createFail(row, no, name, "존재하지 않는 학과 번호");
		}
		
		return null;
	}
	private StudentExcelResult.FailItem createFail(int rowNum, String studentNo, String studentName, String reason) {
		StudentExcelResult.FailItem f = new StudentExcelResult.FailItem();
		f.setRowNum(rowNum + 1);
		f.setStudentNo(studentNo);
		f.setStudentName(studentName);
		f.setReason(reason);
		return f;
	}
	private String getCellValue(Cell c) {
		if(c == null) return null;
		if(c.getCellType() == CellType.STRING) return c.getStringCellValue().trim();
		if(c.getCellType() == CellType.NUMERIC) return String.valueOf((int) c.getNumericCellValue());
		return null;
	}
	// 학생 추가(개별)
	
	// 학생 수정
	
}

