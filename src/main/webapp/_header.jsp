<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- This is a common header fragment --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Election</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <meta charset="UTF-8">
    <link href="css/body.css" rel='stylesheet' type='text/css' />

</head>
<body>
<%
    String headerType = request.getParameter("headerType");
%>
<ul>
    <%
        if ("admin".equals(headerType)) {
            if (session.getAttribute("adminName") != null) {
    %>
    <li><a class="active" href="#" aria-disabled="true">${sessionScope.adminName }</a></li>
    <li style="float:right"><a href="adminLogout.jsp">Logout</a></li>
    <li style="float:right"><a href="adminRegister.jsp">New Admin</a></li>
    <li style="float:right"><a href="addParty.jsp">Party</a></li>
    <li style="float:right"><a href="adminResult.jsp">Results</a></li>
    <li style="float:right"><a href="adminVoter.jsp">Voters</a></li>
    <li style="float:right"><a href="home.jsp">User's Login</a></li>
    <%
            } else {
    %>
    <li><a class="active" href="adminPanel.jsp">Home</a></li>
    <li style="float:right"><a href="adminRegister.jsp">New Admin</a></li>
    <li style="float:right"><a href="home.jsp">User's Login</a></li>
    <%
            }
        } else { // Default to user header if not admin
            if (session.getAttribute("uname") != null) {
    %>
    <li><a class="active" aria-disabled="true">${sessionScope.uname }</a></li>
    <li style="float:right"><a href="logout.jsp">Logout</a></li>
    <li style="float:right"><a href="about.jsp">About</a></li>
    <li style="float:right"><a href="contact.jsp">Contact us</a></li>
    <%
            } else {
    %>
    <li><a class="active" href="home.jsp">Home</a></li>
    <li style="float:right"><a href="adminPanel.jsp">Admin</a></li>
    <li style="float:right"><a href="register.jsp">Signup</a></li>
    <li style="float:right"><a href="about.jsp">About</a></li>
    <li style="float:right"><a href="contact.jsp">Contact us</a></li>
    <%
            }
        }
    %>
</ul>