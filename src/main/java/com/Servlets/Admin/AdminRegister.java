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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@WebServlet(name = "AdminRegister",value = "/AdminRegister")
public class AdminRegister extends HttpServlet {

    @SuppressWarnings("unused")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String fullName=request.getParameter("name");
        String password=request.getParameter("password");

        Model m=new Model();
        m.setFullName(fullName);

        try {
            // Generate a cryptographically secure salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String base64Salt = Base64.getEncoder().encodeToString(salt);

            // Hash the password with the salt using SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            String base64HashedPassword = Base64.getEncoder().encodeToString(hashedPassword);

            // Store the hashed password and salt in the model (assuming Model has setPasswordHash and setSalt methods)
            m.setPasswordHash(base64HashedPassword);
            m.setSalt(base64Salt);

            int i= Dao.registerAdmin(m);
            if(i!=0){
                response.sendRedirect("adminPanel.jsp?msg=success");
            }else {
                response.sendRedirect("adminRegister.jsp?msg=failed");
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            response.sendRedirect("adminRegister.jsp?msg=error");
        } catch (Exception e){
            e.printStackTrace();
            response.sendRedirect("adminRegister.jsp?msg=error");
        }
    }
}