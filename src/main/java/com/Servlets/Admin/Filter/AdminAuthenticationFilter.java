package com.Servlets.Admin.Filter;

import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * This Java filter demonstrates how to intercept the request
 * and transform the response to implement authentication feature.
 * for the website's back-end.
 *
 * @author www.codejava.net
 */
@WebFilter("/admin/*")
public class AdminAuthenticationFilter extends AbstractAuthenticationFilter {

    @Override
    protected String getUserSessionAttributeName() {
        return "uname";
    }

    @Override
    protected String getLoginURI(HttpServletRequest httpRequest) {
        return httpRequest.getContextPath() + "/Admin/AdminLogin";
    }

    @Override
    protected String getLoginPagePath() {
        return "AdminLogin.jsp";
    }

    @Override
    protected String getDefaultLoggedInRedirectPath() {
        return "/Admin/";
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest httpRequest) {
        // For the admin filter, any request that is not explicitly the login servlet
        // or login JSP page requires authentication if the user is not logged in.
        String loginURI = getLoginURI(httpRequest);
        boolean isLoginRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean isLoginPage = httpRequest.getRequestURI().endsWith(getLoginPagePath());
        return !(isLoginRequest || isLoginPage);
    }
}