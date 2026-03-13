package com.Servlets.Voter;

import com.Dao.Dao;
// import com.Model.Model; // No longer used after SQL injection fix

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection; // NEW
import java.sql.PreparedStatement; // NEW
import java.sql.ResultSet;


@WebServlet(name = "Login",value = "/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Login() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        if (action == null) {
            request.getRequestDispatcher("home.jsp").forward(request, response);
        } else {
            if (action.equalsIgnoreCase("logout")) {
                session.removeAttribute("voterId");
                session.removeAttribute("uname");
                response.sendRedirect("home.jsp");
            }
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session= request.getSession();
        @SuppressWarnings("unused")
        String action=request.getParameter("action");

        String voterId = request.getParameter("voter_card_number");
        String password = request.getParameter("password");

        // Connect to mysql and verify username password
        // The Model object and direct SQL string concatenation are removed
        // to prevent SQL injection.
        // Model m=new Model();
        // m.setPass(password);
        // m.setVoterId(voterId);
        // String sql="select voter_card_number,password,username from login where voter_card_number='"+voterId+"' and password='"+password+"'";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Assuming Dao.getConnection() exists and provides a database connection.
            // This allows PreparedStatement to be handled directly in the servlet.
            conn = Dao.getConnection();
            String sql = "select voter_card_number,password,username from login where voter_card_number=? and password=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, voterId);
            ps.setString(2, password);

            rs = ps.executeQuery();

            // ResultSet rs= Dao.voterValid(m); // Original commented out line
            // ResultSet rs=Dao.valid1(sql); // Original vulnerable line

            if(rs.next()){
                String username= "Welcome "+rs.getString(3);
                session.setAttribute("voterId",voterId);
                session.setAttribute("uname",username);
                request.getRequestDispatcher("voter.jsp").forward(request,response);
                //response.sendRedirect("voter.jsp");
            }else{
                request.setAttribute("error", "Invalid Account");
                response.sendRedirect("home.jsp?msg=invalid");
                //request.getRequestDispatcher("home.jsp?msg=invalid").forward(request, response);
                //response.sendRedirect("error.jsp");
            }
        }catch (Exception e){
            e.printStackTrace();
            response.sendRedirect("home.jsp?msg=error");
        } finally {
            // Close database resources safely
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}