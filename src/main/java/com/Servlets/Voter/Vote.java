package com.Servlets.Voter;

import com.Dao.Dao;
import com.Model.Model;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Vote", value = "/Vote")
public class Vote extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String voterId = request.getParameter("voter_card_number");
        String vote = request.getParameter("voter");

        // --- Start of changes for wp-10: Adding basic input validation and robust error handling ---
        // These checks enhance the integrity of the voting process by ensuring
        // essential voter information is provided before attempting to process the vote.
        if (voterId == null || voterId.trim().isEmpty()) {
            response.sendRedirect("voter.jsp?msg=missingVoterId");
            return; // Stop processing if voter ID is missing
        }
        if (vote == null || vote.trim().isEmpty()) {
            response.sendRedirect("voter.jsp?msg=missingVote");
            return; // Stop processing if vote is missing
        }
        // --- End of changes for wp-10 ---

        Model m = new Model();
        m.setVoterId(voterId);
        m.setVote(vote);
        try {
            int i = Dao.votePublish(m);
            if (i != 0) {
                response.sendRedirect("successVoter.jsp");
            } else {
                // The Dao.votePublish method is expected to return 0 if the vote
                // is invalid due to reasons like the voter already having voted
                // or the selected party not existing.
                response.sendRedirect("voter.jsp?msg=invalid");
                //response.sendRedirect("failVoter.jsp");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Redirect to a generic error page or log the error for investigation
            response.sendRedirect("voter.jsp?msg=error");
        }
    }
}