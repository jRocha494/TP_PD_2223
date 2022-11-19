package Server.jdbc;

import Models.User;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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

    public void initializeDatabase() throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, nome FROM utilizador WHERE nome = 'admin'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        if(!resultSet.isBeforeFirst())
        {
            createAdmin();
        }

        resultSet.close();
        statement.close();
    }

    public void updateUser(int id, HashMap<String, String> updateUserMap) throws SQLException
    {
        Statement statement = dbConn.createStatement();
        
        String sqlQuery = "UPDATE utilizador SET ";
        int index=0;
        for (Map.Entry<String, String> entry : updateUserMap.entrySet()) {
            sqlQuery += entry.getKey() + "='" + entry.getValue();
            if (++index < updateUserMap.size())
                sqlQuery += "', ";
        }
        sqlQuery += "' WHERE id=" + id;

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

    public User authenticateUser(String username, String password) throws SQLException {
        String sqlQuery = "SELECT * FROM utilizador " +
                "WHERE username = '" + username +
                "' AND password = '" + password + "';";

        try(
            Statement statement = dbConn.createStatement();
        ) {
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            User user = null;
            while(resultSet.next()) {
                user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("nome"),
                        resultSet.getString("password"),
                        resultSet.getInt("administrador"),
                        resultSet.getInt("autenticado"));
                sqlQuery = "UPDATE utilizador SET autenticado = 1 WHERE id = " + user.getId();
                statement.executeUpdate(sqlQuery);
                System.out.println(user.getUsername() + " has logged in");
            }

            return user;
            //TODO: see if errors are caught by a try catch block
//            throw new Error()
        }catch (SQLException e){
            e.printStackTrace();
            throw e;
        }
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

    private void createAdmin() throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "INSERT INTO utilizador (username, nome, password, administrador) VALUES ('admin','admin','admin', 1)";
        statement.executeUpdate(sqlQuery);
        statement.close();
    }
}
