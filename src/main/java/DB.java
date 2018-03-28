/**
 * Created by afer on 2017/7/26.
 */

import java.sql.*;


//String sql = "CREATE TABLE BDATA " +
//        "(ID INTEGER PRIMARY KEY  AUTOINCREMENT," +
//        " type INT NOT NULL, " +
//        " datastr TEXT NOT NULL, " +
//        " time TEXT NOT NULL)";

public class DB {

    private Connection connectDB(){
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:data.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return c;
    }


    public void insert(int type, String datastr, String time) {
        String sql = "INSERT INTO BDATA(type, datastr, time) VALUES(?,?,?)";

        try {
            Connection conn = this.connectDB();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, type);
            pstmt.setString(2, datastr);
            pstmt.setString(3, time);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
