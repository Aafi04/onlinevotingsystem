package com.Servlets.Voter;

import com.Model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VoteTest {

    private Vote voteServlet;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private Vote.VotePublisher mockVotePublisher;

    @BeforeEach
    void setUp() {
        voteServlet = new Vote();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockVotePublisher = mock(Vote.VotePublisher.class);

        // Inject the mock publisher into the servlet
        voteServlet.setVotePublisher(mockVotePublisher);

        // Common request parameters for most tests
        when(mockRequest.getParameter("voter_card_number")).thenReturn("VOTER123");
        when(mockRequest.getParameter("voter")).thenReturn("PartyA");
    }

    @Test
    void testSuccessfulVoteOneTime() throws ServletException, IOException, Exception {
        // Simulate a successful vote (votePublish returns non-zero)
        when(mockVotePublisher.publishVote(any(Model.class))).thenReturn(1);

        voteServlet.doPost(mockRequest, mockResponse);

        // Verify that the votePublisher was called with the correct model
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(mockVotePublisher).publishVote(modelCaptor.capture());
        assertEquals("VOTER123", modelCaptor.getValue().getVoterId());
        assertEquals("PartyA", modelCaptor.getValue().getVote());

        // Verify redirect to success page
        verify(mockResponse).sendRedirect("successVoter.jsp");
    }

    @Test
    void testSubsequentVoteRejected() throws ServletException, IOException, Exception {
        // Simulate a subsequent vote attempt being rejected (votePublish returns 0)
        when(mockVotePublisher.publishVote(any(Model.class))).thenReturn(0);

        voteServlet.doPost(mockRequest, mockResponse);

        // Verify that the votePublisher was called
        verify(mockVotePublisher).publishVote(any(Model.class));

        // Verify redirect to invalid voter page
        verify(mockResponse).sendRedirect("voter.jsp?msg=invalid");
    }

    @Test
    void testDatabaseErrorHandling() throws ServletException, IOException, Exception {
        // Simulate a SQLException during vote publishing
        when(mockVotePublisher.publishVote(any(Model.class))).thenThrow(new SQLException("Database connection failed"));

        voteServlet.doPost(mockRequest, mockResponse);

        // Verify that the votePublisher was called
        verify(mockVotePublisher).publishVote(any(Model.class));

        // Verify redirect to database error page
        verify(mockResponse).sendRedirect("error.jsp?msg=dbError");
    }

    @Test
    void testInvalidPartyVote() throws ServletException, IOException, Exception {
        // Simulate voting for a non-existent party resulting in rejection (votePublish returns 0)
        // This behavior is currently indistinguishable from a subsequent vote rejection
        // based on the `Dao.votePublish` return value (0 for failure, non-zero for success).
        when(mockVotePublisher.publishVote(any(Model.class))).thenReturn(0);

        // Change request parameter to simulate an invalid party (though the servlet logic
        // doesn't differentiate based on party name for the return value of Dao.votePublish)
        when(mockRequest.getParameter("voter")).thenReturn("NonExistentParty");

        voteServlet.doPost(mockRequest, mockResponse);

        // Verify that the votePublisher was called with the correct model
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(mockVotePublisher).publishVote(modelCaptor.capture());
        assertEquals("VOTER123", modelCaptor.getValue().getVoterId());
        assertEquals("NonExistentParty", modelCaptor.getValue().getVote());

        // Verify redirect to invalid voter page (as per current servlet logic for `i == 0`)
        verify(mockResponse).sendRedirect("voter.jsp?msg=invalid");
    }
}