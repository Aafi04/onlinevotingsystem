```diff
--- a/src/main/java/com/Dao/Dao.java
+++ b/src/main/java/com/Dao/Dao.java
@@ -8,6 +8,8 @@
 import java.io.InputStream;
 import java.sql.*;
 import java.util.Base64;
+import java.util.HashMap;
+import java.util.Map;
 
 public class Dao {
     static Connection con = null;
@@ -194,4 +196,69 @@
         result = ps.executeUpdate();
         return  result;
     }
+
+    /**
+     * Registers a new admin with a hashed password and salt.
+     * This method assumes the 'admin' table has columns 'username', 'password', and 'salt'.
+     * The 'password' column will store the hashed password, and 'salt' will store the salt,
+     * both typically Base64 encoded strings.
+     *
+     * @param username The admin's username.
+     * @param hashedPassword The Base64 encoded hashed password.
+     * @param salt The Base64 encoded salt used for hashing.
+     * @return The number of rows affected (1 for success, 0 for failure).
+     * @throws SQLException If a database access error occurs.
+     */
+    public static int registerAdmin(String username, String hashedPassword, String salt) throws SQLException {
+        int result = 0;
+        String sql = "insert into admin(username,password,salt) values(?,?,?)";
+        PreparedStatement ps = null;
+        try {
+            ps = con.prepareStatement(sql);
+            ps.setString(1, username);
+            ps.setString(2, hashedPassword);
+            ps.setString(3, salt);
+            result = ps.executeUpdate();
+        } finally {
+            if (ps != null) {
+                ps.close();
+            }
+        }
+        return result;
+    }
+
+    /**
+     * Checks if an admin username already exists in the database.
+     *
+     * @param username The username to check.
+     * @return true if the username exists, false otherwise.
+     * @throws SQLException If a database access error occurs.
+     */
+    public static boolean adminUsernameExists(String username) throws SQLException {
+        String sql = "select count(*) from admin where username=?";
+        PreparedStatement ps = null;
+        ResultSet rs = null;
+        try {
+            ps = con.prepareStatement(sql);
+            ps.setString(1, username);
+            rs = ps.executeQuery();
+            if (rs.next()) {
+                return rs.getInt(1) > 0;
+            }
+        } finally {
+            if (rs != null) {
+                rs.close();
+            }
+            if (ps != null) {
+                ps.close();
+            }
+        }
+        return false;
+    }
+
+    /**
+     * Retrieves admin login credentials (adminId, username, hashed password, and salt)
+     * for a given username.
+     *
+     * @param username The username of the admin.
+     * @return A Map containing admin credentials if found, otherwise null.
+     * @throws SQLException If a database access error occurs.
+     */
+    public static Map<String, String> getAdminLoginCredentials(String username) throws SQLException {
+        String sql = "select adminId, username, password, salt from admin where username=?";
+        PreparedStatement ps = null;
+        ResultSet rs = null;
+        Map<String, String> credentials = null;
+        try {
+            ps = con.prepareStatement(sql);
+            ps.setString(1, username);
+            rs = ps.executeQuery();
+            if (rs.next()) {
+                credentials = new HashMap<>();
+                credentials.put("adminId", String.valueOf(rs.getInt("adminId")));
+                credentials.put("username", rs.getString("username"));
+                credentials.put("hashedPassword", rs.getString("password"));
+                credentials.put("salt", rs.getString("salt"));
+            }
+        } finally {
+            if (rs != null) {
+                rs.close();
+            }
+            if (ps != null) {
+                ps.close();
+            }
+        }
+        return credentials;
+    }
 }
```