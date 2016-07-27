package main.databaseservice;

import java.sql.*;
import java.util.Date;

import com.mysql.jdbc.MySQLConnection;

//"jdbc:mysql://localhost/ingestassistantmainbase?autoReconnect=true&useSSL=false"

public class SQLClass {
    String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    String DB_URL;
    final static String USER = "tizen";
    final static String PASS = "tizen";
    Connection SQL_Connection = null;
    Statement Statement = null;
    ResultSet rs;
    ResultSetMetaData md;// = rs.getMetaData();

    public static final String CON_PATH = "jdbc:mysql://192.168.1.10/maindb?autoReconnect=true&useSSL=false";

    public static SQLClass Create_Class(String Con_Path) {
        SQLClass cl = new SQLClass();
        try {
            cl.JDBC_DRIVER = "com.mysql.jdbc.Driver";
            Class.forName("com.mysql.jdbc.Driver");
            cl.DB_URL = Con_Path;
            if (cl.Open_Connection())
                if (cl.Create_Statement()) return cl;
                else return null;
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean Create_Statement() {
        try {
            Statement = SQL_Connection.createStatement();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean Open_Connection() {
        try {
            SQL_Connection = DriverManager.getConnection(DB_URL, USER, PASS);
            return true;
        } catch (Exception ex) {
            return false;
        }


    }

    public boolean Read_All(String Zap) {
        try {
            rs = Statement.executeQuery(Zap);
            md = rs.getMetaData();
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    public static Object ReadObject(String Zap, String Con_Path) {
        try {
            SQLClass cl = SQLClass.Create_Class(Con_Path);
            cl.rs = cl.Statement.executeQuery(Zap);
            Object ret = null;
            while (cl.rs.next()) {
                ret = cl.rs.getObject(1);
            }
            cl.Close_All();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean Execute(String Zap, String Con_Path) {
        try {
            SQLClass cl = SQLClass.Create_Class(Con_Path);
            cl.Statement.executeUpdate(Zap);
            cl.Close_All();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static Integer Insert(String query, String Con_Path) {
        SQLClass cl = new SQLClass();
        Integer numero = 0;
        try {
            cl.JDBC_DRIVER = "com.mysql.jdbc.Driver";
            Class.forName("com.mysql.jdbc.Driver");
            cl.DB_URL = Con_Path;
            cl.SQL_Connection = DriverManager.getConnection(cl.DB_URL, USER, PASS);
            cl.Statement = cl.SQL_Connection.createStatement();

            PreparedStatement pstmt = cl.SQL_Connection.prepareStatement(query, cl.Statement.RETURN_GENERATED_KEYS);
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            keys.next();
            numero = keys.getInt(1);


            //numero = cl.Statement.executeUpdate(query, cl.Statement.RETURN_GENERATED_KEYS);


            cl.Close_All();


        } catch (Exception e) {
            e.printStackTrace();

        }
        return numero;
    }

    public boolean Close_All() {
        try {


            rs.close();
            Statement.close();
            SQL_Connection.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
