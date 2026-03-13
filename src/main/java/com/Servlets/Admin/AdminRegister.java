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

@WebServlet(name = "AdminRegister",value = "/AdminRegister")
public class AdminRegister extends HttpServlet {

    @SuppressWarnings("unused")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String fullName=request.getParameter("name");
        String password=request.getParameter("password");

        Model m=new Model();
        m.setPass(password);
        m.setFullName(fullName);
        // The original `sql` string was vulnerable to SQL injection due to direct concatenation of user input.
        // It has been converted to use parameter markers (`?`) suitable for a PreparedStatement,
        // addressing the issue of constructing a database query with user-controlled input.
        // Note: The actual database interaction occurs via `Dao.registerAdmin(m)`,
        // and this `sql` string variable is currently not directly used in the execution path.
        String sql="insert into admin(username,password) values(?,?)";

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