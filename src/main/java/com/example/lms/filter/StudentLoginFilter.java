package com.example.lms.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StudentLoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        System.out.println("StudentLoginFilter 실행됨: " + uri);

        //  로그인 & 정적 리소스는 필터 제외
        if (
            uri.equals("/") ||
            uri.startsWith("/login") ||
            uri.startsWith("/css") ||
            uri.startsWith("/js") ||
            uri.startsWith("/images")
        ) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("loginStudent") == null) {
            resp.sendRedirect("/login");
            return;
        }

        chain.doFilter(request, response);
    }
}
