package com.example.lms.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmpLoginFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        log.debug("EmpLoginFilter 실행됨: " + uri);

        // ✅ 로그인 & 정적 리소스 & 학생/교수 페이지는 필터 제외
        if (
            uri.equals("/") ||
            uri.startsWith("/public") ||
            uri.startsWith("/login") ||
            uri.startsWith("/css") ||
            uri.startsWith("/js") ||
            uri.startsWith("/images") ||
            uri.startsWith("/student") ||
            uri.startsWith("/professor")
        ) {
            chain.doFilter(request, response);
            return;
        }

           
        // 직원 전용 페이지 접근 시 세션 검사
        if (uri.startsWith("/emp")) { 
	        HttpSession session = req.getSession(false);
	        if (session == null || session.getAttribute("loginEmp") == null) {
	        	log.debug("EmpLoginFilter 접근 거부: 직원 로그인 세션 없음. URI={}", uri);
	            resp.sendRedirect("/login");
	            
	            return;
	        }
        }
        
        log.debug("접근 허용: 직원 로그인 확인됨. URI={}", uri);

        chain.doFilter(request, response);
    
	}

}
