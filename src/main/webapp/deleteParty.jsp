<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link href="css/body.css" rel='stylesheet' type='text/css' />
</head>
<body >
<jsp:include page="adminHeader.jsp"></jsp:include>

<%@ page import="java.sql.*" %>
<%@ page import="com.Dao.Dao" %>
<%@ page import="java.util.UUID" %> <%-- Added for CSRF token generation --%>
<%
String input=null;
String message= request.getParameter("msg");

// CSRF token generation and storage in session
String csrfToken = (String) session.getAttribute("csrfToken");
if (csrfToken == null) {
    csrfToken = UUID.randomUUID().toString();
    session.setAttribute("csrfToken", csrfToken);
}
%>


<div class="limiter">
    <br><br><br><br>

    <div class="container-login100">
        <div class="wrap-login100">

            <form action="deleteParty.jsp" method="post" style="max-width:350px;margin:auto" >
                <center>
                    <div class="container" style="width: 400px">
                        <div style="align-content: center">
                            <ul style="align-content: center">
                                <li style="text-align: center"><a href="addParty.jsp">Add Party</a></li>
                                <li style="text-align: center"><a  href="viewParty.jsp">View</a></li>
                                <li style="text-align: center"><a class="active" href="deleteParty.jsp">Delete</a></li>

                            </ul>
                        </div>
                        <h1>Remove Party</h1>
                        <p>To remove party enter Party Code</p>
                        <hr>
                        <input type="text" placeholder="Party Code" name="party_code" required>
                        <%-- Add CSRF token hidden field --%>
                        <input type="hidden" name="csrfToken" value="<%= csrfToken %>">

                        <hr>
                        <button type="submit" class="btn">Submit</button>

                        <%
// Retrieve the CSRF token from session for validation against the submitted token
String currentCsrfToken = (String) session.getAttribute("csrfToken");

input=request.getParameter("party_code");
// Only process deletion if it's a POST request and party_code is provided
if (request.getMethod().equalsIgnoreCase("POST") && input!=null) {
    String submittedCsrfToken = request.getParameter("csrfToken");

    // Validate the CSRF token
    if (submittedCsrfToken != null && submittedCsrfToken.equals(currentCsrfToken)) {
        // CSRF token is valid, proceed with deletion
        int pid = Dao.getId(input);

        Connection con = null;
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/evoting", "root", "root");
        String sql = "delete from partytable where pid=?";

        PreparedStatement statement = con.prepareStatement(sql);
        statement.setInt(1, pid);
        int rs = statement.executeUpdate();

        if (rs != 0) {
            response.sendRedirect("deleteParty.jsp?msg=success");
        } else {
            response.sendRedirect("deleteParty.jsp?msg=failed");
        }
        // For per-session tokens, it's not strictly necessary to regenerate after each use,
        // but for single-use tokens, it would be done here.
    } else {
        // CSRF token mismatch or missing, redirect with an error message
        response.sendRedirect("deleteParty.jsp?msg=csrfFailed");
    }
}
%>


                    </div>
                    <div class="container signin">
                        <% if(message!=null){
                            if(message.equals("success")){%>
                        <img src="images/ok-16.png" alt="Computer Man" style="width:23px;height:23px;">  <font color="#1B9B3E">Deleted successfully </font>
                        <%}else if(message.equals("failed")){
                        %>
                        <img src="images/alert-16.png" alt="Computer Man" style="width:23px;height:23px;" autofocus> <font color="#ff0000">Failed to Delete</font>
                        <%}else if(message.equals("csrfFailed")){ // New message type for CSRF validation failure
                        %>
                        <img src="images/alert-16.png" alt="Computer Man" style="width:23px;height:23px;" autofocus> <font color="#ff0000">Security check failed. Please try again.</font>
                        <%}}%>

                        </div>

                </center>
            </form>

        </div>
        <jsp:include page="adminFooter.jsp"></jsp:include>
    </div>
</div>
</body>
</html>