package com.Servlets.Admin;

import com.Dao.Dao;
import com.Model.Model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64; // Import Base64 for decoding salt

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

    // Helper method to verify a password against a stored hash and salt
    private boolean verifyPassword(String password, String storedSaltAndHash) {
        try {
            // Split the stored string into salt and hash parts
            String[] parts = storedSaltAndHash.split("\\$");
            if (parts.length != 2) {
                System.err.println("Invalid stored password format.");
                return false; // Stored format is incorrect
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]); // Decode Base64 salt
            String storedHexHash = parts[1]; // Get the stored hex hash

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt); // Add the retrieved salt to the digest
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8")); // Hash the input password

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b)); // Convert byte to hex string
            }
            String newHexHash = sb.toString();

            return newHexHash.equals(storedHexHash); // Compare the generated hash with the stored hash
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | IllegalArgumentException e) {
            // Log the exception for debugging purposes
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessionAdmin = request.getSession();
        String action = request.getParameter("action");
        if (action == null) {
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        } else {
            if (action.equalsIgnoreCase("logout")) {
                // Invalidate the session for a more secure logout
                sessionAdmin.invalidate();
                response.sendRedirect("adminPanel.jsp");
            }
        }
    }

    @SuppressWarnings("static-access")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessionAdmin = request.getSession();
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Dao dao = new Dao();
        Model model = new Model();
        model.setUserName(username);
        // model.setPass(password); // Do not set raw password here for direct DB comparison

        try {
            // Assuming dao.adminValid now retrieves the admin record by username
            // and returns a ResultSet containing the stored password (salt$hash)
            ResultSet rs = dao.adminValid(model);
            if (rs.next()) {
                String storedSaltAndHash = rs.getString("password"); // Retrieve the stored salt$hash
                if (verifyPassword(password, storedSaltAndHash)) {
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
        } catch (Exception e) {
            e.printStackTrace();
            // Generic error handling
            request.setAttribute("message", "error");
            request.getRequestDispatcher("adminPanel.jsp").forward(request, response);
        }
    }
}