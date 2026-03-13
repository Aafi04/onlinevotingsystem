<!DOCTYPE html>
<%@page import="java.sql.*" %>
<%@ page import="java.util.HashMap" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/body.css" rel='stylesheet' type='text/css'/>
    <c:if test="${sessionScope.adminName != null}">
        <link rel="stylesheet" type="text/css" href="styles.css">
    </c:if>
</head>
<body>

<%
    // Initialize map for counts
    HashMap<String, Integer> partyCounts = new HashMap<>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver"); // Standardize to newer driver
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/evoting", "root", "root");
        stmt = con.createStatement();
        rs = stmt.executeQuery("select voter, count(voter) as c from voter group by voter");

        while (rs.next()) {
            partyCounts.put(rs.getString("voter"), rs.getInt("c")); // Use getInt for counts
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // Close resources gracefully
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignore) {
            }
        }
    }

    // Set partyCounts as a request attribute for JSTL access
    request.setAttribute("partyCounts", partyCounts);
%>

<%-- Dynamically include adminHeader or regular header based on session --%>
<jsp:include page="${sessionScope.adminName != null ? 'adminHeader.jsp' : 'header.jsp'}"></jsp:include>
<div class="limiter">
    <div class="container-login100">
        <div class="wrap-login100">
            <form action="" method="post" style="max-width:1000px;margin:auto">
                <center>
                    <div class="container">
                        <%-- Public View: Display image-based results if not an admin --%>
                        <c:if test="${sessionScope.adminName == null}">
                            <h1>Result</h1>
                            <hr>
                            <div>
                                <div class="reg" style="padding-top: 35px;">
                                    <center>
                                        <img src="images/bbjp.png" height="90x"
                                             width="90x"/> ${partyCounts.BJP}
                                        <img src="images/inc.png" height="90x"
                                             width="90x"/> ${partyCounts.INC}
                                        <img src="images/aap.png" height="90x"
                                             width="90x"/> ${partyCounts.AAP}
                                        <img src="images/bsp.jpg" height="90x"
                                             width="90x"/> ${partyCounts.BSP}
                                        <img src="images/cpi.png" height="90x"
                                             width="90x"/> ${partyCounts.CPI}
                                    </center>
                                </div>
                                <hr>
                            </div>
                        </c:if>

                        <%-- Admin View: Display table-based results if logged in as admin --%>
                        <c:if test="${sessionScope.adminName != null}">
                            <h1>Voting Results</h1>
                            <table border="1">
                                <tr>
                                    <th>Voter</th>
                                    <th>Count</th>
                                </tr>
                                <c:forEach var="entry" items="${partyCounts}">
                                    <tr>
                                        <td><c:out value="${entry.key}"/></td>
                                        <td><c:out value="${entry.value}"/></td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </c:if>

                    </div>
                    <div class="container signin">
                    </div>
                </center>
            </form>
        </div>
        <%-- Dynamically include adminFooter or regular footer based on session --%>
        <jsp:include page="${sessionScope.adminName != null ? 'adminFooter.jsp' : 'footer.jsp'}"></jsp:include>
    </div>
</div>
</body>
</html>