```diff
--- a/src/main/java/com/Dao/Dao.java
+++ b/src/main/java/com/Dao/Dao.java
@@ -4,21 +4,91 @@
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.*;
+import java.util.Arrays;
 import java.util.Base64;
+import java.util.Properties;
+import java.io.FileInputStream;
+import java.io.FileNotFoundException;
+import java.security.NoSuchAlgorithmException;
+import java.security.SecureRandom;
+import java.security.spec.InvalidKeySpecException;
+import javax.crypto.SecretKeyFactory;
+import javax.crypto.spec.PBEKeySpec;
 
 public class Dao {
-    static Connection con = null;
+    private static Connection con = null;
+    private static final String PROPERTIES_FILE = "db.properties";
+    private static final int ITERATIONS = 65536;
+    private static final int KEY_LENGTH = 256;
+    private static final int SALT_LENGTH = 16;
+
     static
     {
-        try
-        {
-            Class.forName("com.mysql.cj.jdbc.Driver");
-            //database_name --> evoting
-            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/evoting", "root", "root");
-        }
-        catch (Exception e)
-        {
+        Properties props = new Properties();
+        try (InputStream input = Dao.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
+            if (input == null) {
+                System.err.println("Sorry, unable to find " + PROPERTIES_FILE + ". Database connection may fail.");
+                // Optionally throw a runtime exception here if the properties file is mandatory
+                throw new FileNotFoundException("Properties file not found: " + PROPERTIES_FILE);
+            }
+            props.load(input);
+
+            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
+            con = DriverManager.getConnection(
+                    props.getProperty("db.url"),
+                    props.getProperty("db.username"),
+                    props.getProperty("db.password")
+            );
+        } catch (FileNotFoundException e) {
+            System.err.println("Error: " + PROPERTIES_FILE + " not found. Database connection cannot be established securely.");
+            e.printStackTrace();
+        } catch (Exception e) {
             e.printStackTrace();
         }
     }
+
+    public static Connection getConnection() {
+        return con;
+    }
+
+    private static byte[] generateSalt() throws NoSuchAlgorithmException {
+        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
+        byte[] salt = new byte[SALT_LENGTH];
+        random.nextBytes(salt);
+        return salt;
+    }
+
+    private static String hashPassword(String password, byte[] salt)
+            throws NoSuchAlgorithmException, InvalidKeySpecException {
+        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
+        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
+        byte[] hash = skf.generateSecret(spec).getEncoded();
+        return Base64.getEncoder().encodeToString(hash);
+    }
+
+    public static boolean verifyPassword(String candidatePassword, String storedHashAndSalt)
+            throws NoSuchAlgorithmException, InvalidKeySpecException {
+        if (storedHashAndSalt == null || !storedHashAndSalt.contains(":")) {
+            return false;
+        }
+
+        String[] parts = storedHashAndSalt.split(":");
+        if (parts.length != 2) {
+            return false;
+        }
+
+        byte[] salt = Base64.getDecoder().decode(parts[0]);
+        String storedHash = parts[1];
+
+        String generatedHash = hashPassword(candidatePassword, salt);
+        return generatedHash.equals(storedHash);
+    }
     public static ResultSet loginValidation(String sql) throws SQLException{
         PreparedStatement ps=con.prepareStatement(sql);
         ResultSet rs  = ps.executeQuery();
         return rs;
     }
-    public static ResultSet adminValid(Model m) throws SQLException{
-        String sql="select adminId,username,password from admin where username=? and password=?";
+@@ -23,14 +87,26 @@
+    }
+
+    public static ResultSet adminValid(Model m) throws SQLException{
+        String sql="select adminId,username,password from admin where username=?";
 
         PreparedStatement ps=con.prepareStatement(sql);
         ps.setString(1,m.getUserName());
-        ps.setString(2,m.getPass());
 
         ResultSet rs= ps.executeQuery();
-        return rs;
+
+        if (rs.next()) {
+            String storedHashAndSalt = rs.getString("password");
+            try {
+                if (verifyPassword(m.getPass(), storedHashAndSalt)) {
+                    return rs;
+                }
+            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
+                System.err.println("Error verifying admin password: " + e.getMessage());
+                e.printStackTrace();
+            }
+        }
+        return null;
     }
 
     public static ResultSet voterValid(Model m) throws SQLException{
         String sql="select voter_card_number,password,username from login where voter_card_number=? and password=?";
         PreparedStatement ps=con.prepareStatement(sql);
         ps.setString(1, m.getVoterId());
         ps.setString(2,m.getPass());
         ResultSet rs  = ps.executeQuery();
         return rs;
     }
+
+@@ -37,11 +113,32 @@
+     public static ResultSet voterValid(Model m) throws SQLException{
+         String sql="select voter_card_number,password,username from login where voter_card_number=? and password=?";
+         PreparedStatement ps=con.prepareStatement(sql);
+         ps.setString(1, m.getVoterId());
+         ps.setString(2,m.getPass());
+         ResultSet rs  = ps.executeQuery();
+         return rs;
+     }
+
+    public static ResultSet voterValidSecure(Model m) throws SQLException {
+        String sql="select voter_card_number,password,username from login where voter_card_number=?";
+        PreparedStatement ps=con.prepareStatement(sql);
+        ps.setString(1, m.getVoterId());
+        ResultSet rs  = ps.executeQuery();
+
+        if (rs.next()) {
+            String storedHashAndSalt = rs.getString("password");
+            try {
+                if (verifyPassword(m.getPass(), storedHashAndSalt)) {
+                    return rs;
+                }
+            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
+                System.err.println("Error verifying voter password: " + e.getMessage());
+                e.printStackTrace();
+            }
+        }
+        return null;
+    }
+
     public static ResultSet valid1(String sql) throws SQLException{
         PreparedStatement ps=con.prepareStatement(sql);
         ResultSet rs  = ps.executeQuery();
@@ -53,7 +170,24 @@
         return result;
     }
 
-    public static int register(Model m) throws SQLException{
+@@ -50,16 +145,24 @@
+     public static int register(Model m) throws SQLException{
         int result =0;
         String sql="insert into login(voter_card_number,name,username,gender,dob,email,password) values(?,?,?,?,?,?,?)";
         PreparedStatement ps=con.prepareStatement(sql);
         ps.setString(1, m.getVoterId());
         ps.setString(2, m.getName());
         ps.setString(3, m.getUserName());
         ps.setString(4, m.getGender());
         ps.setString(5, m.getDob());
         ps.setString(6, m.getEmail());
-        ps.setString(7, m.getPass());
+
+        try {
+            byte[] salt = generateSalt();
+            String hashedPassword = hashPassword(m.getPass(), salt);
+            String storedHashAndSalt = Base64.getEncoder().encodeToString(salt) + ":" + hashedPassword;
+            ps.setString(7, storedHashAndSalt);
+        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
+            System.err.println("Error hashing password for voter registration: " + e.getMessage()); e.printStackTrace(); return 0; }
 
         result = ps.executeUpdate();
         return  result;
@@ -131,13 +205,19 @@
         return 0;
     }
 
-    public static int registerAdmin(Model m) throws SQLException{
+@@ -130,10 +169,18 @@
+     public static int registerAdmin(Model m) throws SQLException{
         int result =0;
         String sql="insert into admin(username,password) values(?,?)";
         PreparedStatement ps=con.prepareStatement(sql);
         ps.setString(1, m.getFullName());
-        ps.setString(2, m.getPass());
 
+        try {
+            byte[] salt = generateSalt();
+            String hashedPassword = hashPassword(m.getPass(), salt);
+            String storedHashAndSalt = Base64.getEncoder().encodeToString(salt) + ":" + hashedPassword;
+            ps.setString(2, storedHashAndSalt);
+        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
+            System.err.println("Error hashing password for admin registration: " + e.getMessage()); e.printStackTrace(); return 0; }
         result = ps.executeUpdate();
         return  result;
     }
```