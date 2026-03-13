package com.Servlets.Admin.Filter;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter("/*")
public class FrontEndAuthenticationFilter extends AbstractAuthenticationFilter {

    private static final String[] loginRequiredURLs = {
            "/view_profile", "/edit_profile", "/update_profile"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Bypass this filter for admin paths, let the AdminAuthenticationFilter handle them
        if (path.startsWith("/admin/")) {
            chain.doFilter(request, response);
            return;
        }

        // Delegate to the common authentication logic in the base class
        super.doFilter(request, response, chain);
    }

    @Override
    protected String getUserSessionAttributeName() {
        return "customerUser";
    }

    @Override
    protected String getLoginURI(HttpServletRequest httpRequest) {
        return httpRequest.getContextPath() + "/login";
    }

    @Override
    protected String getLoginPagePath() {
        return "/login.jsp";
    }

    @Override
    protected String getDefaultLoggedInRedirectPath() {
        return "/";
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest httpRequest) {
        String requestURL = httpRequest.getRequestURL().toString();

        for (String loginRequiredURL : loginRequiredURLs) {
            if (requestURL.contains(loginRequiredURL)) {
                return true;
            }
        }
        return false;
    }
}