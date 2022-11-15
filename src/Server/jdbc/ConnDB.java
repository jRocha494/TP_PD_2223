package Server.jdbc;

import java.sql.*;
import java.util.Scanner;

public class ConnDB
{
    private final String DATABASE_URL = "jdbc:sqlite:PD-2022-23-TP.db";
    private Connection dbConn;

    public ConnDB() throws SQLException
    {
        dbConn = DriverManager.getConnection(DATABASE_URL);
    }

    public void close() throws SQLException
    {
        if (dbConn != null)
            dbConn.close();
    }

    public void listUsers(String whereName) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, name, birthdate FROM users";
        if (whereName != null)
            sqlQuery += " WHERE name like '%" + whereName + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            Date birthdate = resultSet.getDate("birthdate");
            System.out.println("[" + id + "] " + name + " (" + birthdate + ")");
        }

        resultSet.close();
        statement.close();
    }

    public void updateUser(int id, String name, String birthdate) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "UPDATE users SET name='" + name + "', " +
                            "BIRTHDATE='" + birthdate + "' WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void deleteUser(int id) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "DELETE FROM users WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void createUser(String name, String username, String password) throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "INSERT INTO utilizador (username, nome, password) VALUES ('" + username + "','" + name + "','" + password + "')";
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void readUser() throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, nome, username, password FROM utilizador";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("nome");
            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            System.out.println("[" + id + "] " + name + " (" + username + ")" + password);
        }

        resultSet.close();
        statement.close();
    }
}
