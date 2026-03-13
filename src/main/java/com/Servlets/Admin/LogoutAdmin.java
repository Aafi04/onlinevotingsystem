package com.Servlets.Admin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "LogoutAdmin",value = "/LogoutAdmin")
public class LogoutAdmin extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LogoutAdmin() {
        super();
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        performLogout(request, response, "adminName", "adminPanel.jsp");
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private static void performLogout(HttpServletRequest request, HttpServletResponse response, String cookieName, String redirectPage) throws IOException {
        HttpSession session = request.getSession(true);
        session.invalidate();

        // to expire a cookie
        Cookie c = new Cookie(cookieName, "");
        response.addCookie(c);
        Cookie[] c1 = request.getCookies();
        if (c1 != null && c1.length > 0) {
            c1[0].setMaxAge(0);
        }

        response.sendRedirect(redirectPage);
    }
}