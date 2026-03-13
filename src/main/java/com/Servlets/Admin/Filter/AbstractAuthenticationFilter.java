package com.Servlets.Admin.Filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract class AbstractAuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No-op by default, subclasses can override if needed
    }

    @Override
    public void destroy() {
        // No-op by default, subclasses can override if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);

        boolean isLoggedIn = (session != null && session.getAttribute(getUserSessionAttributeName()) != null);

        String loginURI = getLoginURI(httpRequest);
        boolean isLoginRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean isLoginPage = httpRequest.getRequestURI().endsWith(getLoginPagePath());

        if (isLoggedIn && (isLoginRequest || isLoginPage)) {
            // The user is already logged in and tries to access login page.
            // Redirect to their default logged-in page.
            RequestDispatcher dispatcher = request.getRequestDispatcher(getDefaultLoggedInRedirectPath());
            dispatcher.forward(request, response);
        } else if (!isLoggedIn && requiresAuthentication(httpRequest)) {
            // The user is not logged in and the requested page requires authentication.
            // Forward to the login page.
            RequestDispatcher dispatcher = request.getRequestDispatcher(getLoginPagePath());
            dispatcher.forward(request, response);
        } else {
            // For other requested pages that do not require authentication
            // or the user is already logged in, continue to the destination.
            chain.doFilter(request, response);
        }
    }

    /**
     * Returns the name of the session attribute that stores the user object.
     */
    protected abstract String getUserSessionAttributeName();

    /**
     * Returns the full URI path to the login servlet/page.
     * E.g., "/contextPath/Admin/AdminLogin"
     */
    protected abstract String getLoginURI(HttpServletRequest httpRequest);

    /**
     * Returns the path to the login JSP page.
     * E.g., "AdminLogin.jsp" or "/login.jsp"
     */
    protected abstract String getLoginPagePath();

    /**
     * Returns the default path to redirect to when a user is already logged in
     * and tries to access the login page.
     * E.g., "/Admin/" or "/"
     */
    protected abstract String getDefaultLoggedInRedirectPath();

    /**
     * Determines if the current request requires user authentication.
     */
    protected abstract boolean requiresAuthentication(HttpServletRequest httpRequest);
}