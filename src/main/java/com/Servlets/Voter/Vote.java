package com.Servlets.Voter;

import com.Dao.Dao;
import com.Model.Model;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException; // Added for specific database error handling

@WebServlet(name = "Vote", value = "/Vote")
public class Vote extends HttpServlet {

    // Package-private functional interface to abstract the vote publishing logic.
    // This allows for mocking the static Dao.votePublish method during testing.
    interface VotePublisher {
        int publishVote(Model m) throws Exception;
    }

    // Default implementation uses the static Dao.votePublish method.
    // In production, this will be used.
    private VotePublisher votePublisher = (m) -> Dao.votePublish(m);

    // Package-private setter for dependency injection during testing.
    // This allows test cases to provide a mock implementation of VotePublisher.
    void setVotePublisher(VotePublisher publisher) {
        this.votePublisher = publisher;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String voterId = request.getParameter("voter_card_number");
        String vote = request.getParameter("voter");
        Model m = new Model();
        m.setVoterId(voterId);
        m.setVote(vote);
        try {
            // Use the injected votePublisher to perform the vote operation.
            int i = votePublisher.publishVote(m);
            if (i != 0) {
                response.sendRedirect("successVoter.jsp");
            } else {
                response.sendRedirect("voter.jsp?msg=invalid");
            }
        } catch (SQLException e) {
            // Handle database-specific errors gracefully.
            e.printStackTrace(); // Log the error for debugging
            response.sendRedirect("error.jsp?msg=dbError"); // Redirect to a generic error page
        } catch (Exception e) {
            // Catch any other unexpected exceptions.
            e.printStackTrace(); // Log the error for debugging
            response.sendRedirect("error.jsp?msg=generalError"); // Redirect to a generic error page
        }
    }
}