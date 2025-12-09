package com.example.lms.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class StudentLoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        log.debug("StudentLoginFilter 실행됨: " + uri);

        //  로그인 & 정적 리소스는 필터 제외
        if (
            uri.equals("/") ||
            uri.startsWith("/public") ||
            uri.startsWith("/login") ||
            uri.startsWith("/css") ||
            uri.startsWith("/js") ||
            uri.startsWith("/images") ||
            uri.startsWith("/professor") ||  
            uri.startsWith("/emp")
        ) {
            chain.doFilter(request, response);
            return;
        }
        
        // 학생 전용 페이지 접근 시 세션 검사
        if (uri.startsWith("/student")) { 
        	HttpSession session = req.getSession(false);
	        if (session == null || session.getAttribute("loginStudent") == null) {
	        	log.debug("StudentLoginFilter 접근 거부: 학생 로그인 세션 없음. URI={}", uri);
	            resp.sendRedirect("/login");
	            return;
	        }
        
        }
        log.debug("접근 허용: 학생 로그인 확인됨. URI={}", uri);

        chain.doFilter(request, response);
    }
}
