package com.Servlets.Admin.Voters;

import com.Dao.Dao;
import com.Model.Model;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name ="Voters",value = "/Voters")
public class Voters extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String requestCsrfToken = request.getParameter("csrfToken");
        String sessionCsrfToken = (String) session.getAttribute("csrfToken");

        // Validate CSRF token
        if (requestCsrfToken == null || sessionCsrfToken == null || !requestCsrfToken.equals(sessionCsrfToken)) {
            response.sendRedirect("adminVoter.jsp?msg=csrfFailed");
            return; // Stop further processing
        }

        // Token is valid, remove it from session to prevent replay attacks
        session.removeAttribute("csrfToken");

        String voterId = request.getParameter("voterId");
        System.out.println(voterId);
        // Connect to mysql and verify username password
        Model m=new Model();
        m.setVoterId(voterId);
        try {
            int rs= Dao.deleteVoter(voterId);
            if(rs!=0){
                response.sendRedirect("adminVoter.jsp?msg=success");
            }else{
                response.sendRedirect("adminVoter.jsp?msg=failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            response.sendRedirect("adminVoter.jsp?msg=error"); // Redirect on unexpected error
        }
    }
}