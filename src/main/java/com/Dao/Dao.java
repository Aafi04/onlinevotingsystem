package com.Dao;


import com.Model.Model;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Base64;
import java.util.Properties;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Dao {
    static Connection con = null;
    private static final String DB_PROPERTIES_FILE = "/db.properties"; // Path within classpath
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    static
    {
        try
        {
            // Load database properties
            Properties props = new Properties();
            try (InputStream input = Dao.class.getResourceAsStream(DB_PROPERTIES_FILE)) {
                if (input == null) {
                    System.err.println("CRITICAL SECURITY: Unable to find " + DB_PROPERTIES_FILE + ". Falling back to hardcoded credentials.");
                    dbUrl = "jdbc:mysql://localhost:3306/evoting";
                    dbUser = "root";
                    dbPassword = "root";
                } else {
                    props.load(input);
                    dbUrl = props.getProperty("db.url");
                    dbUser = props.getProperty("db.username");
                    dbPassword = props.getProperty("db.password");
                    if (dbUrl == null || dbUser == null || dbPassword == null) {
                        System.err.println("CRITICAL SECURITY: Missing database properties (db.url, db.username, or db.password) in " + DB_PROPERTIES_FILE + ". Falling back to hardcoded credentials.");
                        dbUrl = "jdbc:mysql://localhost:3306/evoting";
                        dbUser = "root";
                        dbPassword = "root";
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("CRITICAL SECURITY: Error loading properties file. Falling back to hardcoded credentials.");
                dbUrl = "jdbc:mysql://localhost:3306/evoting";
                dbUser = "root";
                dbPassword = "root";
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            //database_name --> evoting
            con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("CRITICAL SECURITY: Database connection failed. Please check database configuration and credentials.");
        }
    }

    // Helper method to generate a salt
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 bytes = 128 bits
        random.nextBytes(salt);
        return salt;
    }

    // Helper method to hash a password with a given salt
    private static String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    // Helper method to hash a password and generate a new salt
    private static String hashPasswordWithNewSalt(String password) throws NoSuchAlgorithmException {
        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        // Store salt and hash concatenated, both Base64 encoded for storage as a single string
        return Base64.getEncoder().encodeToString(salt) + "$" + hashedPassword;
    }

    // Helper method to verify a password
    private static boolean verifyPassword(String providedPassword, String storedSaltAndHash) throws NoSuchAlgorithmException {
        if (storedSaltAndHash == null || !storedSaltAndHash.contains("$")) {
            return false; // Invalid stored format or no hash stored
        }

        String[] parts = storedSaltAndHash.split("\\$");
        if (parts.length != 2) {
            return false; // Invalid format
        }

        byte[] storedSalt = Base64.getDecoder().decode(parts[0]);
        String storedHash = parts[1];

        String providedPasswordHashed = hashPassword(providedPassword, storedSalt);
        return providedPasswordHashed.equals(storedHash);
    }

    public static ResultSet loginValidation(String sql) throws SQLException{
        PreparedStatement ps=con.prepareStatement(sql);
        ResultSet rs  = ps.executeQuery();
        return rs;
    }
    public static ResultSet adminValid(Model m) throws SQLException{
        String sql="select adminId,username,password from admin where username=? and password=?";

        PreparedStatement ps=con.prepareStatement(sql);
        ps.setString(1,m.getUserName());
        ps.setString(2,m.getPass());

        ResultSet rs= ps.executeQuery();
        return rs;
    }

    public static ResultSet voterValid(Model m) throws SQLException{
        String sql = "select voter_card_number,password,username from login where voter_card_number=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, m.getVoterId());
            rs = ps.executeQuery();

            if (rs.next()) { // User found by voter ID
                String storedSaltAndHash = rs.getString("password");
                try {
                    if (verifyPassword(m.getPass(), storedSaltAndHash)) {
                        // Password matches. The rs is currently positioned at the correct row.
                        // Return this rs for the caller to process.
                        // The caller is responsible for closing this ResultSet and its associated PreparedStatement.
                        return rs;
                    } else {
                        // Password mismatch.
                        // Close the current ResultSet and PreparedStatement.
                        rs.close();
                        ps.close();
                        // Return an empty ResultSet with the expected columns for no match.
                        return con.createStatement().executeQuery("SELECT voter_card_number,password,username FROM login WHERE 1=0");
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    throw new SQLException("Error verifying password due to hashing algorithm issue.", e);
                }
            } else {
                // No user found by voter ID.
                // Close the current ResultSet and PreparedStatement.
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                // Return an empty ResultSet with the expected columns for no user.
                return con.createStatement().executeQuery("SELECT voter_card_number,password,username FROM login WHERE 1=0");
            }
        } finally {
            // Note: Resources ps and rs are explicitly closed within the try block
            // if an empty ResultSet is returned. If a non-empty rs is returned,
            // it is the caller's responsibility to close it along with its ps.
        }
    }


    public static ResultSet valid1(String sql) throws SQLException{
        PreparedStatement ps=con.prepareStatement(sql);
        ResultSet rs  = ps.executeQuery();
        return rs;
    }

    public static int votePublish(Model m) throws SQLException{
        int result=0;
        String sql="select voter_card_number from login where voter_card_number=?";
        PreparedStatement ps= null;
        ResultSet rs=null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, m.getVoterId());
            rs = ps.executeQuery();
            while (rs.next()){
                String sql2="insert ignore into voter(voter_card_number,voter) values(?,?)";
                PreparedStatement preparedStatement=null;
                try {
                    preparedStatement = con.prepareStatement(sql2);
                    preparedStatement.setString(1, m.getVoterId());
                    preparedStatement.setString(2,m.getVote());
                    result=preparedStatement.executeUpdate();
                } finally {
                    if (preparedStatement != null) preparedStatement.close();
                }
                return result;
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
        return result;
    }

    public static int register(Model m) throws SQLException{
        int result =0;
        String sql="insert into login(voter_card_number,name,username,gender,dob,email,password) values(?,?,?,?,?,?,?)";
        PreparedStatement ps=null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, m.getVoterId());
            ps.setString(2, m.getFullName());
            ps.setString(3, m.getUserName());
            ps.setString(4, m.getGender());
            ps.setString(5, m.getDob());
            ps.setString(6, m.getEmail());
            
            try {
                String saltedHashedPassword = hashPasswordWithNewSalt(m.getPass());
                ps.setString(7, saltedHashedPassword);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new SQLException("Failed to register user: Error hashing password.", e);
            }

            result = ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
        return  result;
    }


    public static int contact(Model m) throws SQLException{
        int result =0;
        String sql="insert into contact(name,company,email,message) values(?,?,?,?)";
        PreparedStatement ps=null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getCompanyName());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getMessage());

            result =ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
        return result;

    }
    public static Model getPic(int  id) throws SQLException, IOException{
        Model model=null;

        String sql="Select * from partytable WHERE pid=?";

        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1,id);
            rs = ps.executeQuery();

            if(rs.next()){
                model=new Model();
                String partyCode=rs.getString("partyCode");
                String partyName=rs.getString("partyName");
                Blob blob =rs.getBlob("photo");

                InputStream inputStream=blob.getBinaryStream();
                ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                byte[] buffer= new byte[4096];
                int bytesRead =-1;

                while ((bytesRead=inputStream.read(buffer))!=-1){
                    outputStream.write(buffer,0,bytesRead);
                }

                byte[] imageBytes = outputStream.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);


                inputStream.close();
                outputStream.close();

               model.setPartyCode(partyCode);
               model.setPartyName(partyName);
               model.setBase64Image(base64Image);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
        return model;
    }

    public static int getId(String partyCode) throws SQLException{
        String sql="select pid from partytable Where partyCode=?";
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1,partyCode);
            rs = ps.executeQuery();
            while (rs.next()){
                return rs.getInt(1);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
        return 0;
    }

    public static int registerAdmin(Model m) throws SQLException{
        int result =0;
        String sql="insert into admin(username,password) values(?,?)";
        PreparedStatement ps=null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getPass());

            result = ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
        return  result;
    }
    public static int deleteVoter(String voterid) throws SQLException{
        String sql="delete from login where voter_card_number=?";
        PreparedStatement ps=null;
        int result = 0;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1,voterid);
            result = ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
        return  result;

    }

    public static int register(String sql) throws SQLException{
        int result =0;
        PreparedStatement ps=null;
        try {
            ps = con.prepareStatement(sql);
            result = ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
        return  result;
    }
}