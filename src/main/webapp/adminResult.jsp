<%@ page import="java.sql.*" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map.Entry" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Results</title>
    <link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
<jsp:include page="adminHeader.jsp"></jsp:include>

<%
    String s1 = (String) session.getAttribute("adminName");
    if (s1 == null) {
        response.sendRedirect("adminPanel.jsp");
        return;
    }

    HashMap<String, String> count = new HashMap<String, String>();
    HashMap<String, Integer> countInt = new HashMap<String, Integer>();

    Connection con = null;
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/evoting", "root", "root");

        String sql = "select * from partytable";
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery("select voter, count(voter) as c from voter group by voter");

        while (resultSet.next()) {
            count.put(resultSet.getString("voter"), resultSet.getString("c"));
            countInt.put(resultSet.getString("voter"), Integer.parseInt(resultSet.getString("c")));
        }
        // Make the map available to JSTL
        request.setAttribute("resultsMap", countInt);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
%>

<div class="limiter">
    <div class="container">
        <h1>Voting Results</h1>
        <table border="1">
            <tr>
                <th>Voter</th>
                <th>Count</th>
            </tr>
            <c:forEach var="entry" items="${resultsMap}">
                <tr>
                    <td><c:out value="${entry.key}"/></td>
                    <td><c:out value="${entry.value}"/></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>

<jsp:include page="adminFooter.jsp"></jsp:include>
</body>
</html>
=== src/main/webapp/adminVoter.jsp ===
--- a/src/main/webapp/adminVoter.jsp
+++ b/src/main/webapp/adminVoter.jsp
@@ -1,6 +1,7 @@
 <%@ page contentType="text/html;charset=UTF-8" language="java" %>
 <%@ page import="java.sql.*"%>
-<html>
+<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
+<html lang="en">
 <head>
     <meta name="viewport" content="width=device-width, initial-scale=1">
     <link href="css/body.css" rel="stylesheet" type="text/css" />
@@ -25,27 +26,29 @@
         PreparedStatement statement=con.prepareStatement(sql);
         statement.setString(1, input);
         ResultSet rs=statement.executeQuery();%>
-    <form action="Voters" method="post" name="delete">
-    <table class="table-all">
-        <tr>
-            <th>Name</th>
-            <th>VoterId</th>
-        </tr>
-        <%
-        while (rs.next()) {
-            String voterId = rs.getString(1);
-            String name = rs.getString(2);
-
-%>
-
-        <tr>
-            <td> <%=name%></td>
-            <td> <input name="voterId" value="<%=voterId%>" autocomplete="<%=voterId%>"></td>
-            <td> <button type="submit">delete </button></td>
-
-        </tr>
-    </table>
-
+        <form action="Voters" method="post" name="delete">
+            <table class="table-all">
+                <tr>
+                    <th>Name</th>
+                    <th>VoterId</th>
+                    <th>Action</th>
+                </tr>
+                <%
+                while (rs.next()) {
+                    // Set voter details as page attributes for JSTL to access
+                    pageContext.setAttribute("voterId", rs.getString(1));
+                    pageContext.setAttribute("voterName", rs.getString(2));
+                %>
+                <tr>
+                    <td> <c:out value="${voterName}"/></td>
+                    <td> <input name="voterId" value="<c:out value="${voterId}"/>" autocomplete="off" readonly></td>
+                    <td> <button type="submit">delete </button></td>
+                </tr>
+                <%
+                } // End of while loop
+                %>
+            </table>
+        </form>
     <%
             }
     }
@@ -62,7 +65,6 @@
 
         </div>
     </form>
-    <div style="width: 100%;height: 50vh">
 
     </div>
 <div style="align-content: end">