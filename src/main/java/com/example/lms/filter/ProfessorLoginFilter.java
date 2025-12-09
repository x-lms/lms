package com.example.lms.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProfessorLoginFilter implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        log.debug("ProfessorLoginFilter 실행됨: " + uri);

        // ✅ 로그인 & 정적 리소스 & 학생/직원 페이지는 필터 제외
        if (
            uri.equals("/") ||
            uri.startsWith("/public") ||
            uri.startsWith("/login") ||
            uri.startsWith("/css") ||
            uri.startsWith("/js") ||
            uri.startsWith("/images") ||
            uri.startsWith("/student") ||
            uri.startsWith("/emp")
        ) {
            chain.doFilter(request, response);
            return;
        }

           
        // 교수 전용 페이지 접근 시 세션 검사
        if (uri.startsWith("/professor")) { 
	        HttpSession session = req.getSession(false);
	        if (session == null || session.getAttribute("loginProfessor") == null) {
	        	log.debug("ProfessorLoginFilter 접근 거부: 교수 로그인 세션 없음. URI={}", uri);
	            resp.sendRedirect("/login");
	            
	            return;
	        }
        }
        
        log.debug("접근 허용: 교수 로그인 확인됨. URI={}", uri);

        chain.doFilter(request, response);
    
	}

}
