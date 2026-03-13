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
import java.util.Base64;

@WebServlet(name = "AdminRegister",value = "/AdminRegister")
public class AdminRegister extends HttpServlet {

    // Helper method to hash the password with a randomly generated salt
    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16]; // Generate a 16-byte salt
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt); // Add salt to the digest
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8")); // Hash password with UTF-8 encoding

            // Encode salt to Base64 and hash to Hex for storage
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b)); // Convert byte to hex string
            }
            String hexHash = sb.toString();

            return encodedSalt + "$" + hexHash; // Store salt and hash combined
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            // Log the exception or throw a custom runtime exception
            System.err.println("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    @SuppressWarnings("unused")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String fullName=request.getParameter("name");
        String password=request.getParameter("password");

        String hashedPassword = hashPassword(password); // Hash the password

        Model m=new Model();
        m.setPass(hashedPassword); // Store the hashed password
        m.setFullName(fullName);
        String sql="insert into admin(username,password) values('"+fullName+"','"+password+"')"; // This line is not used by Dao.registerAdmin, kept as-is.

        String message=null;
        try {
            int i= Dao.registerAdmin(m);
            if(i!=0){
                response.sendRedirect("adminPanel.jsp?msg=success");

                //out.println("success");
                //response.flushBuffer();
                //TimeUnit.SECONDS.sleep(2);
                //response.sendRedirect("successAdminRegister.jsp");

            }else {
                response.sendRedirect("adminRegister.jsp?msg=failed");
                //out.println("fail");
                //response.flushBuffer();
                //TimeUnit.SECONDS.sleep(20);
                //response.sendRedirect("failAdminRegister.jsp");

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}