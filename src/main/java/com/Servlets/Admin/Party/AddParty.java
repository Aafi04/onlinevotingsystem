package com.Servlets.Admin.Party;

+import com.Dao.Dao;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
@@ -40,7 +41,7 @@
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
-import java.sql.DriverManager;
+// import java.sql.DriverManager; // Removed unused import
 import java.sql.PreparedStatement;


 @WebServlet(name = "AddParty",value = "/AddParty")
@@ -58,12 +59,12 @@
             inputStream=filePart.getInputStream();
         }
 
+        Connection con = null;
         try {
-            Connection con=null;
-            Class.forName("com.mysql.cj.jdbc.Driver");
-            //database_name --> evoting
-            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/evoting", "root", "root");
-
+            // Use the centralized Dao.getConnection() method
+            con = Dao.getConnection();
+            // No need for Class.forName or DriverManager.getConnection here anymore
+            
             String sql="INSERT INTO partyTable (partyCode, partyName, photo) values (?, ?, ?)";
             PreparedStatement ps=con.prepareStatement(sql);
             ps.setString(1,partyCode);
@@ -74,13 +75,19 @@
             int result=ps.executeUpdate();
             if(result!=0){
                 response.sendRedirect("addParty.jsp?msg=success");
-            }else {
+            } else {
                 response.sendRedirect("addParty.jsp?msg=failed");
-                //response.sendRedirect("failAddParty.jsp");
             }
 
-        }catch (Exception e){
+        } catch (Exception e){
             e.printStackTrace();
+            response.sendRedirect("addParty.jsp?msg=error");
+        } finally {
+            if (con != null) {
+                try {
+                    con.close();
+                } catch (java.sql.SQLException e) {
+                    e.printStackTrace();
+                }
+            }
         }
-
     }
 }