package com.Servlets.Admin;

import com.Dao.Dao;
import com.Model.Model;

import java.io.IOException;
import java.sql.ResultSet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "AdminLogin", value="/AdminLogin")
public class AdminLogin extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
                sessionAdmin.removeAttribute("adminId");
                sessionAdmin.removeAttribute("adminName");
                response.sendRedirect("adminPanel.jsp");
            }
        }
    }

    @SuppressWarnings("static-access")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessionAdmin = request.getSession();
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Assuming Dao and Model classes are used for database operations
        Dao dao = new Dao();
        Model model = new Model();
        model.setUserName(username);

        try {
            ResultSet rs = dao.adminValid(model); // This now fetches password_hash and salt
            if (rs.next()) {
                String storedHash = rs.getString("password_hash"); // Assuming column name 'password_hash'
                String storedSalt = rs.getString("salt");           // Assuming column name 'salt'

                // Decode salt from Base64
                byte[] saltBytes = Base64.getDecoder().decode(storedSalt);

                // Hash the provided password with the retrieved salt
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(saltBytes);
                byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
                String base64HashedPassword = Base64.getEncoder().encodeToString(hashedPassword);

                // Compare the newly generated hash with the stored hash
                if (base64HashedPassword.equals(storedHash)) {
                    sessionAdmin.setAttribute("adminId", rs.getInt("adminId"));
                    sessionAdmin.setAttribute("adminName", rs.getString("username"));
                    response.sendRedirect("adminPanel.jsp"); // Ensure this path is correct
                } else {
                    // Password mismatch
                    request.setAttribute("message", "invalid");
                    request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
                }
            } else {
                // User not found
                request.setAttribute("message", "invalid");
                request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            request.setAttribute("message", "error");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "error");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        }
    }
}