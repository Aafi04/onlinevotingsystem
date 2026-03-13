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
        // Renamed 'vote' parameter to 'partyId' for clarity, as it represents the party the voter is voting for.
        String partyId = request.getParameter("voter");
        Model m = new Model();
        m.setVoterId(voterId);
        m.setVote(partyId); // Assuming Model.setVote expects the party ID

        try {
            // The Dao.votePublish method is assumed to return specific integer codes:
            // 1: Success
            // -1: Voter has already cast a vote
            // -2: Attempt to vote for a non-existent party ID
            // Any other value (e.g., 0): Generic failure
            int result = Dao.votePublish(m);

            if (result == 1) {
                // Vote successfully recorded
                response.sendRedirect("successVoter.jsp");
            } else if (result == -1) {
                // Voter has already cast a vote
                response.sendRedirect("voter.jsp?msg=alreadyVoted");
            } else if (result == -2) {
                // Attempt to vote for a non-existent party ID
                response.sendRedirect("voter.jsp?msg=partyNotFound");
            } else {
                // Generic failure, or an unexpected return code from Dao.votePublish
                response.sendRedirect("voter.jsp?msg=invalid");
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            // Redirect to an error page indicating a database or server issue
            response.sendRedirect("voter.jsp?msg=dbError");
        }
    }
}