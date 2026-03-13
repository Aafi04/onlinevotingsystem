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

    // Extracted core logic for improved testability.
    // This method encapsulates the business logic of processing a vote,
    // making it easier to test in isolation if a testing framework
    // allowed invoking private methods or if it were made protected for subclassing.
    // Returns a non-zero integer for success, and 0 for failure (e.g., invalid voter,
    // already voted, non-existent party, as determined by Dao.votePublish).
    private int handleVoteLogic(String voterId, String partyVote) throws Exception {
        Model m = new Model();
        m.setVoterId(voterId);
        m.setVote(partyVote); // Assuming 'vote' parameter is the party identifier
        return Dao.votePublish(m);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String voterId = request.getParameter("voter_card_number");
        String vote = request.getParameter("voter"); // This is the party ID or name

        try {
            int result = handleVoteLogic(voterId, vote);

            if (result != 0) {
                response.sendRedirect("successVoter.jsp");
            } else {
                // This branch handles business logic failures such as:
                // - Voter attempting to vote a second time.
                // - Voter submitting a vote for a party ID that does not exist.
                response.sendRedirect("voter.jsp?msg=invalid");
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes.
            e.printStackTrace();
            // Redirect to a generic error page for system-level exceptions.
            response.sendRedirect("error.jsp?msg=system_error");
        }
    }
}