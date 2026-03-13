package com.Servlets.Admin;

import com.Dao.Dao;
import com.Model.Model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

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

        // Server-side validation: Check for empty fields
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("message", "emptyFields");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
            return;
        }

        try {
            // Retrieve admin credentials (hashed password and salt) from the DAO
            Map<String, String> adminCredentials = Dao.getAdminLoginCredentials(username);

            if (adminCredentials != null && !adminCredentials.isEmpty()) {
                String storedHashedPasswordBase64 = adminCredentials.get("hashedPassword");
                String storedSaltBase64 = adminCredentials.get("salt");

                // Ensure both hash and salt are present
                if (storedHashedPasswordBase64 == null || storedSaltBase64 == null) {
                    request.setAttribute("message", "invalid"); // Data integrity issue for this user
                    request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
                    return;
                }

                // Decode stored salt and hashed password from Base64
                byte[] storedSalt = Base64.getDecoder().decode(storedSaltBase64);
                byte[] storedHashedPassword = Base64.getDecoder().decode(storedHashedPasswordBase64);

                // Hash the incoming password with the retrieved salt
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(storedSalt);
                byte[] incomingHashedPassword = md.digest(password.getBytes("UTF-8"));

                // Compare the newly generated hash with the stored hash
                if (Arrays.equals(incomingHashedPassword, storedHashedPassword)) {
                    sessionAdmin.setAttribute("adminId", Integer.parseInt(adminCredentials.get("adminId")));
                    sessionAdmin.setAttribute("adminName", adminCredentials.get("username"));
                    response.sendRedirect("adminPanel.jsp"); // Login successful
                } else {
                    request.setAttribute("message", "invalid"); // Password mismatch
                    request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
                }
            } else {
                request.setAttribute("message", "invalid"); // Username not found
                request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the database error
            request.setAttribute("message", "dbError");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Log hashing algorithm error
            request.setAttribute("message", "hashingError");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Log encoding error
            request.setAttribute("message", "encodingError");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            e.printStackTrace(); // Log error if adminId cannot be parsed
            request.setAttribute("message", "dataError");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } catch (Exception e) { // Catch any other unexpected exceptions
            e.printStackTrace();
            request.setAttribute("message", "error");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        }
    }
}