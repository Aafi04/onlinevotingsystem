package com.Servlets;

import com.Model.Model;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractRegistrationServlet extends HttpServlet {

    /**
     * Helper class to encapsulate the result of pre-registration checks.
     */
    protected static class PreRegistrationResult {
        private boolean passed;
        private String redirectUrl;

        public PreRegistrationResult(boolean passed, String redirectUrl) {
            this.passed = passed;
            this.redirectUrl = redirectUrl;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        // Perform any specific pre-registration checks (e.g., age validation for voters)
        PreRegistrationResult preResult = performPreRegistrationChecks(request);
        if (!preResult.isPassed()) {
            response.sendRedirect(preResult.getRedirectUrl());
            return;
        }

        // Create and populate the Model object with data from the request
        Model model = createAndPopulateModel(request);

        try {
            // Execute the specific registration logic via DAO
            int result = executeRegistration(model);
            if (result != 0) {
                response.sendRedirect(getSuccessRedirectUrl());
            } else {
                response.sendRedirect(getFailureRedirectUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // In case of any exception during DAO operation, treat as a failure
            response.sendRedirect(getFailureRedirectUrl());
        }
    }

    /**
     * Creates and populates a Model object with data from the request.
     * This method is responsible for extracting specific parameters for the registration type.
     *
     * @param request The HttpServletRequest.
     * @return A populated Model object.
     */
    protected abstract Model createAndPopulateModel(HttpServletRequest request);

    /**
     * Executes the specific DAO registration method for the Model.
     *
     * @param model The populated Model object.
     * @return An integer representing the result of the registration (non-zero for success, 0 for failure).
     * @throws Exception if a DAO operation fails.
     */
    protected abstract int executeRegistration(Model model) throws Exception;

    /**
     * Returns the URL to redirect to upon successful registration.
     *
     * @return The success redirect URL.
     */
    protected abstract String getSuccessRedirectUrl();

    /**
     * Returns the URL to redirect to upon failed registration.
     *
     * @return The failure redirect URL.
     */
    protected abstract String getFailureRedirectUrl();

    /**
     * Performs any pre-registration checks (e.g., age validation).
     * Subclasses can override this to implement specific checks.
     *
     * @param request The HttpServletRequest.
     * @return A PreRegistrationResult indicating if checks passed and, if not, the redirect URL.
     */
    protected PreRegistrationResult performPreRegistrationChecks(HttpServletRequest request) {
        return new PreRegistrationResult(true, null); // Default: no specific checks, always pass
    }
}