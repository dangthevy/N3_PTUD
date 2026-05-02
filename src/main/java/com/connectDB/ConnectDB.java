package com.connectDB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    public static Connection getConnection() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=BanVeTau;encrypt=true;trustServerCertificate=true";
        String user = "sa"; // Thay bằng user của bạn
        String password = "123456"; // Thay bằng password của bạn
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại: " + e.getMessage());
            return null;
        }
    }
}