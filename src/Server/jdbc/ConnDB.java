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

    public void authenticateUser(String username, String password) throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, nome, username, password, autenticado, administrador FROM utilizador " +
                "WHERE username = '" + username + "' AND password = '" + password + "'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        if(resultSet.isBeforeFirst() && resultSet.getInt("autenticado") == 0 && resultSet.getInt("administrador") == 0){
            sqlQuery = "UPDATE utilizador SET autenticado = 1 WHERE id = " + resultSet.getInt("id");
            statement.executeUpdate(sqlQuery);
            System.out.println(resultSet.getInt("username") + " has logged in");
        }

        statement.close();
    }

    public void readAuthenticatedUsers() throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, nome, username, password FROM utilizador WHERE autenticado = 1";

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
