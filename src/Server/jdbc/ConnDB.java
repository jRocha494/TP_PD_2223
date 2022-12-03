package Server.jdbc;

import Models.Booking;
import Models.Seat;
import Models.Show;
import Models.User;

import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConnDB
{
    private Connection dbConn;

    public ConnDB(String DATABASE_URL) throws SQLException
    {
        dbConn = DriverManager.getConnection(DATABASE_URL);
        this.initializeDatabase();
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

    public List<Booking> readBookings(boolean withConfirmedPayment) throws SQLException {
        List<Booking> bookingList = new ArrayList<>();
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM reserva";
        if(withConfirmedPayment){
            sqlQuery += " WHERE pago = 1";
        }else{
            sqlQuery += " WHERE pago = 0";
        }

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            Booking newBooking = new Booking(resultSet.getInt("id"),
                        resultSet.getString("data_hora"),
                        resultSet.getInt("pago"),
                        resultSet.getInt("id_utilizador"),
                        resultSet.getInt("id_espetaculo"));
            bookingList.add(newBooking);
        }

        resultSet.close();
        statement.close();
        return bookingList;
    }

    public List<Show> readShows(HashMap<String, String> filtersMap) throws SQLException {
        List<Show> showList = new ArrayList<>();
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM espetaculo WHERE visivel = 1 ";
        int index=0;
        for (Map.Entry<String, String> entry : filtersMap.entrySet()) {
            if (index++ < filtersMap.size())
                sqlQuery += " AND ";
            if(entry.getKey().equals("duracao"))
                sqlQuery += entry.getKey() + "=" + Integer.parseInt(entry.getValue());
            else
                sqlQuery += entry.getKey() + " like " + entry.getValue();
        }

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            Show newShow = new Show(resultSet.getInt("id"),
                    resultSet.getString("descricao"),
                    resultSet.getString("tipo"),
                    resultSet.getString("data_hora"),
                    resultSet.getInt("duracao"),
                    resultSet.getString("local"),
                    resultSet.getString("localidade"),
                    resultSet.getString("pais"),
                    resultSet.getString("classificacao_etaria"),
                    resultSet.getInt("visivel"));
            showList.add(newShow);
        }

        resultSet.close();
        statement.close();
        return showList;
    }

    public Show selectShow(int showId) throws SQLException {
        Show show = null;

        if(readShowFreeSeats(showId).isEmpty())
            return null;

        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM espetaculo WHERE id = " + showId;

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            show = new Show(resultSet.getInt("id"),
                    resultSet.getString("descricao"),
                    resultSet.getString("tipo"),
                    resultSet.getString("data_hora"),
                    resultSet.getInt("duracao"),
                    resultSet.getString("local"),
                    resultSet.getString("localidade"),
                    resultSet.getString("pais"),
                    resultSet.getString("classificacao_etaria"),
                    resultSet.getInt("visivel"));
        }

        resultSet.close();
        statement.close();

        return show;
    }

    public List<Seat> readShowFreeSeats(int showId) throws SQLException {
        List<Seat> freeSeatsList = new ArrayList<>();
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM lugar WHERE espetaculo_id = " + showId + " AND id not in (SELECT id_lugar FROM reserva_lugar)";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            Seat newSeat = new Seat(resultSet.getInt("id"),
                    resultSet.getString("fila"),
                    resultSet.getString("assento"),
                    resultSet.getFloat("preco"),
                    resultSet.getInt("espetaculo_id")
            );
            freeSeatsList.add(newSeat);
        }

        resultSet.close();
        statement.close();

        return freeSeatsList;
    }

    public List<Seat> readShowSeats(int showId) throws SQLException {
        List<Seat> seatList = new ArrayList<>();
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM lugar WHERE espetaculo_id = " + showId;

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            Seat newSeat = new Seat(resultSet.getInt("id"),
                    resultSet.getString("fila"),
                    resultSet.getString("assento"),
                    resultSet.getFloat("preco"),
                    resultSet.getInt("espetaculo_id")
                    );
            seatList.add(newSeat);
        }

        resultSet.close();
        statement.close();
        return seatList;
    }

    public Seat selectSeat(String chosenRow, String chosenSeat, int showId) throws SQLException {
        Seat seat = null;
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM lugar WHERE espetaculo_id = " + showId +
                " AND id not in (SELECT id_lugar FROM reserva_lugar)" +
                " AND fila = '" + chosenRow + "' AND assento = '" + chosenSeat + "'";
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            seat = new Seat(resultSet.getInt("id"),
                    resultSet.getString("fila"),
                    resultSet.getString("assento"),
                    resultSet.getFloat("preco"),
                    resultSet.getInt("espetaculo_id")
            );
        }

        resultSet.close();
        statement.close();
        return seat;
    }

    public Booking confirmBooking(int showId, List<Seat> selectedSeats, int userId) throws SQLException {
        Booking booking = null;
        Statement statement = dbConn.createStatement();

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String sqlQuery = "INSERT INTO reserva(data_hora, id_utilizador, id_espetaculo) VALUES('" +
                timeStamp + "'," + userId + "," + showId + ")";
        if(statement.executeUpdate(sqlQuery) > 0)
        {
            booking = readUserLastBooking(userId);
            insertBookingSeats(selectedSeats, booking.getId());
        }

        statement.close();
        return booking;
    }

    private void insertBookingSeats(List<Seat> selectedSeats, int bookingId) throws SQLException {
        String insertSql = "INSERT INTO reserva_lugar SELECT ?,? " +
                "WHERE NOT EXISTS (SELECT 1 FROM reserva_lugar WHERE id_lugar = ?)";

        PreparedStatement pstmt = dbConn.prepareStatement(insertSql);
        for (Seat seat : selectedSeats) {
            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, seat.getId());
            pstmt.setInt(3, seat.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
    }

    private Booking readUserLastBooking(int userId) throws SQLException {
        Booking booking = null;
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM reserva WHERE id_utilizador = " + userId + " ORDER BY id DESC LIMIT 1";
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            booking = new Booking(resultSet.getInt("id"),
                    resultSet.getString("data_hora"),
                    resultSet.getInt("pago"),
                    resultSet.getInt("id_utilizador"),
                    resultSet.getInt("id_espetaculo"));
        }

        resultSet.close();
        statement.close();
        return booking;
    }

    private Booking readUserBooking(int bookingId, int userId) throws SQLException {
        Booking booking = null;
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT * FROM reserva WHERE id = " + bookingId + " AND id_utilizador = " + userId + " ORDER BY id DESC LIMIT 1";
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            booking = new Booking(resultSet.getInt("id"),
                    resultSet.getString("data_hora"),
                    resultSet.getInt("pago"),
                    resultSet.getInt("id_utilizador"),
                    resultSet.getInt("id_espetaculo"));
        }

        resultSet.close();
        statement.close();
        return booking;
    }

    public Booking deleteBooking (int bookingId, int userId) throws SQLException{
        Booking booking = null;
        Statement statement = dbConn.createStatement();

        String sqlQuery = "DELETE FROM reserva WHERE pago = 0 AND id_utilizador = " + userId + " AND id = " + bookingId;
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while (resultSet.next()) {
            deleteBookingSeats(bookingId);
            booking = new Booking(resultSet.getInt("id"),
                    resultSet.getString("data_hora"),
                    resultSet.getInt("pago"),
                    resultSet.getInt("id_utilizador"),
                    resultSet.getInt("id_espetaculo"));
        }

        resultSet.close();
        statement.close();
        return booking;
    }

    private void deleteBookingSeats (int bookingId) throws SQLException{
        Statement statement = dbConn.createStatement();

        String sqlQuery = "DELETE FROM reserva_lugar WHERE id_reserva = " + bookingId;
        statement.executeQuery(sqlQuery);

        statement.close();
    }


    public Booking payBooking(int bookingId, int userId) throws SQLException {
        Booking booking = null;
        Statement statement = dbConn.createStatement();

        String sqlQuery = "UPDATE reserva SET pago = 1 WHERE id = " + bookingId + " AND id_utilizador = " + userId;
        if(statement.executeUpdate(sqlQuery) > 0)
        {
            booking = readUserBooking(bookingId, userId);
        }

        statement.close();
        return booking;
    }

    public void makeShowVisible(int selectedShow) throws SQLException {
        Show show = null;
        Statement statement = dbConn.createStatement();

        String sqlQuery = "UPDATE espetaculo SET pago = 1 WHERE id = " + selectedShow;

        statement.close();
    }
}
