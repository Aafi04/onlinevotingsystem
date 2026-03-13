<%@ page contentType="text/html;charset=UTF-8" language="java" %>
+<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <html>
 <head>
     <link href="css/body.css" rel='stylesheet' type='text/css' />
@@ -103,11 +105,11 @@
                             </tr>
                             <% while (rs.next()){
                                 String  partyCode=rs.getString(2);
-                                String  partyName=rs.getString(3);
-
+                                String  partyName=rs.getString(3); // Make partyName available to JSTL
+                                pageContext.setAttribute("currentPartyCode", partyCode);
+                                pageContext.setAttribute("currentPartyName", partyName);
                             %>
                                 <tr>
-                                    <td><%=partyCode%></td>
-                                    <td><%=partyName%></td>
+                                    <td><c:out value="${currentPartyCode}"/></td>
+                                    <td><c:out value="${currentPartyName}"/></td>
                                 </tr>
                             <%}%>
                         </table>