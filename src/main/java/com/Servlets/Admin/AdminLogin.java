package com.Servlets.Admin;

import com.Dao.Dao;
import com.Model.Model;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.logging.Level; // Added for logging
import java.util.logging.Logger; // Added for logging

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "AdminLogin", value="/AdminLogin")
public class AdminLogin extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AdminLogin.class.getName()); // Logger instance

    public AdminLogin() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessionAdmin = request.getSession();
        String action = request.getParameter("action");
        if (action == null) {
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } else {
            if (action.equalsIgnoreCase("logout")) {
                // Invalidate the session on logout to prevent lingering session data
                if (sessionAdmin != null) {
                    sessionAdmin.invalidate();
                }
                response.sendRedirect("adminPanel.jsp");
            }
        }
    }

    @SuppressWarnings("static-access")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Assuming Dao and Model classes are used for database operations
        Dao dao = new Dao();
        Model model = new Model();
        model.setUserName(username);
        model.setPass(password);

        try {
            ResultSet rs = dao.adminValid(model);
            if (rs.next()) {
                // Invalidate the old session to prevent session fixation
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                // Create a new session
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("adminId", rs.getInt("adminId"));
                newSession.setAttribute("adminName", rs.getString("username"));
                response.sendRedirect("adminPanel.jsp"); // Ensure this path is correct
            } else {
                request.setAttribute("message", "invalid");
                request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
            }
        } catch (Exception e) {
            // Log the exception instead of printing stack trace
            logger.log(Level.SEVERE, "Error during admin login for user: " + username, e);
            request.setAttribute("message", "error"); // Optionally set an error message for the user
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        }
    }
}