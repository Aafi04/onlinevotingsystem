package com.Servlets.Admin;

import com.Dao.Dao;
import com.Model.Model;
import com.Servlets.AbstractRegistrationServlet; // Import the new base class

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(name = "AdminRegister",value = "/AdminRegister")
public class AdminRegister extends AbstractRegistrationServlet {

    @Override
    protected Model createAndPopulateModel(HttpServletRequest request) {
        String fullName = request.getParameter("name");
        String password = request.getParameter("password");

        Model m = new Model();
        m.setPass(password);
        m.setFullName(fullName);
        return m;
    }

    @Override
    protected int executeRegistration(Model model) throws Exception {
        return Dao.registerAdmin(model);
    }

    @Override
    protected String getSuccessRedirectUrl() {
        return "adminPanel.jsp?msg=success";
    }

    @Override
    protected String getFailureRedirectUrl() {
        return "adminRegister.jsp?msg=failed";
    }
}