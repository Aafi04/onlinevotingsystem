package com.Servlets.Admin;

import com.Dao.Dao;
import com.Model.Model;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

@WebServlet(name = "AdminRegister",value = "/AdminRegister")
public class AdminRegister extends HttpServlet {

    @SuppressWarnings("unused")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String fullName = request.getParameter("name");
        String password = request.getParameter("password");

        // Server-side validation: Check for empty fields
        if (fullName == null || fullName.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.sendRedirect("adminRegister.jsp?msg=emptyFields");
            return;
        }

        try {
            // Server-side validation: Check for duplicate username
            if (Dao.adminUsernameExists(fullName)) {
                response.sendRedirect("adminRegister.jsp?msg=duplicateUser");
                return;
            }

            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16]; // 16 bytes for salt
            random.nextBytes(salt);

            // Hash the password using SHA-256 with the generated salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt); // Add salt to the message digest
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8")); // Hash password

            // Encode salt and hashed password to Base64 for storage in the database
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHashedPassword = Base64.getEncoder().encodeToString(hashedPassword);

            // Register admin with the hashed password and salt
            int i = Dao.registerAdmin(fullName, encodedHashedPassword, encodedSalt);

            if (i != 0) {
                response.sendRedirect("adminPanel.jsp?msg=success");
            } else {
                response.sendRedirect("adminRegister.jsp?msg=failed");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Log the database error
            response.sendRedirect("adminRegister.jsp?msg=dbError");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Log hashing algorithm error
            response.sendRedirect("adminRegister.jsp?msg=hashingError");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Log encoding error
            response.sendRedirect("adminRegister.jsp?msg=encodingError");
        } catch (Exception e) { // Catch any other unexpected exceptions
            e.printStackTrace();
            response.sendRedirect("adminRegister.jsp?msg=error");
        }
    }
}