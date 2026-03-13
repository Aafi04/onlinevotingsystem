<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.UUID"%>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/body.css" rel="stylesheet" type="text/css" />
</head>
<body>
<%
    String message=request.getParameter("msg");
    String input =null;
    Connection con=null;
    Class.forName("com.mysql.cj.jdbc.Driver");
    con= DriverManager.getConnection("jdbc:mysql://localhost:3306/evoting", "root", "root");

    // Generate and store a unique anti-CSRF token in the session
    String csrfToken = UUID.randomUUID().toString();
    session.setAttribute("csrfToken", csrfToken);
%>
<jsp:include page="adminHeader.jsp"></jsp:include>
<div class="container">
<hr>
    <hr>
<form action="adminVoter.jsp" method="post" style="max-width:350px;margin:auto">
    <input type="search" name="search" placeholder="Enter voterId" >
    <button type="submit" > Search</button>
</form>
<%
    input =request.getParameter("search");
    if(input !=null){
        String sql="select * from login where voter_card_number=?";

        PreparedStatement statement=con.prepareStatement(sql);
        statement.setString(1, input);
        ResultSet rs=statement.executeQuery();%>
    <%-- Removed the outer form tag for deletion, each delete button will have its own form --%>
    <table class="table-all">
        <tr>
            <th>Name</th>
            <th>VoterId</th>
            <th>Action</th> <%-- Added a column for the delete action --%>
        </tr>
        <%
        while (rs.next()) {
            String voterId = rs.getString(1);
            String name = rs.getString(2);
%>
        <tr>
            <td> <%=name%></td>
            <td> <%=voterId%></td> <%-- Display voterId directly --%>
            <td>
                <form action="Voters" method="post"> <%-- Individual form for each delete action --%>
                    <input type="hidden" name="voterId" value="<%=voterId%>">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>"> <%-- Include CSRF token --%>
                    <button type="submit">delete </button>
                </form>
            </td>
        </tr>
<%
            } // end while
%>
    </table>
<%
    } // end if(input !=null)
%>
    <div class="container signin">
        <% if(message!=null){
            if(message.equals("success")){%>
        <img src="images/ok-16.png" alt="Computer Man" style="width:23px;height:23px;">  <font color="#1B9B3E">Voter Removed Successfully </font>
        <%}else if(message.equals("failed")){
        %>
        <img src="images/alert-16.png" alt="Computer Man" style="width:23px;height:23px;" autofocus> <font color="#ff0000">Voter not Found</font>
        <%}else if(message.equals("csrfFailed")){ // New message for CSRF failure
        %>
        <img src="images/alert-16.png" alt="Computer Man" style="width:23px;height:23px;" autofocus> <font color="#ff0000">Security check failed. Please try again.</font>
        <%}else if(message.equals("error")){ // New message for generic errors
        %>
        <img src="images/alert-16.png" alt="Computer Man" style="width:23px;height:23px;" autofocus> <font color="#ff0000">An unexpected error occurred.</font>
        <%}}%>

        </div>
    <%-- The outer delete form tag was misplaced and is now removed. --%>
    <div style="width: 100%;height: 50vh">

    </div>
<div style="align-content: end">
    <jsp:include page="adminFooter.jsp"></jsp:include>
</div>
</div>

</body>
</html>