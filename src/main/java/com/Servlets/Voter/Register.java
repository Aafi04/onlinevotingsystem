package com.Servlets.Voter;

import com.Dao.Dao;
import com.Model.Model;
import com.Servlets.AbstractRegistrationServlet; // Import the new base class

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@WebServlet(name = "Register",value = "/Register")
public class Register extends AbstractRegistrationServlet {

    @Override
    protected Model createAndPopulateModel(HttpServletRequest request) {
        String voterId = request.getParameter("voter_card_number");
        String fullName = request.getParameter("name");
        String username = request.getParameter("username");
        String gender = request.getParameter("gender");
        String dob = request.getParameter("dob");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Model m = new Model();
        m.setPass(password);
        m.setVoterId(voterId);
        m.setUserName(username);
        m.setDob(dob);
        m.setEmail(email);
        m.setGender(gender);
        m.setFullName(fullName);
        return m;
    }

    @Override
    protected int executeRegistration(Model model) throws Exception {
        return Dao.register(model);
    }

    @Override
    protected String getSuccessRedirectUrl() {
        return "home.jsp?msg=success";
    }

    @Override
    protected String getFailureRedirectUrl() {
        return "register.jsp?msg=failed";
    }

    @Override
    protected PreRegistrationResult performPreRegistrationChecks(HttpServletRequest request) {
        String dob = request.getParameter("dob");
        if (dob == null || dob.length() < 4) {
            return new PreRegistrationResult(false, "register.jsp?msg=invalid_dob");
        }

        String userYearString = dob.substring(0,4);
        Date currentDate = new Date(); // Uses current system date
        String currentYearString = currentDate.toString().substring(24); // Extracts current year from date string

        try {
            int userYear = Integer.parseInt(userYearString);
            int currentYear = Integer.parseInt(currentYearString);

            if (userYear > 1950 && (currentYear - userYear) >= 18) {
                return new PreRegistrationResult(true, null); // Age check passed
            } else {
                return new PreRegistrationResult(false, "register.jsp?msg=age"); // Age check failed
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(); // Log the error
            return new PreRegistrationResult(false, "register.jsp?msg=invalid_dob_format");
        }
    }
}