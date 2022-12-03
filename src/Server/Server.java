package Server;

import Models.*;
import utils.errorHandling.CustomException;
import Server.jdbc.ConnDB;
import utils.errorHandling.ResponseMessage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class Server {
    private ConnDB connDB;
    public Server() {
        try {
            this.connDB = new ConnDB();
            connDB.initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Response registerUser(User user) {
        try {
            connDB.createUser(user.getName(), user.getUsername(), user.getPassword());
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, null);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error registering user", e);
        }
    }

    public Response authenticateUser(User user) {
        try {
            User userAux = connDB.authenticateUser(user.getUsername(), user.getPassword());
            if(userAux == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, userAux);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error authenticating user", e);
        }
    }

    public Response editLoginData(int id, HashMap<String, String> editLoginMap) {
        try {
            connDB.updateUser(id, editLoginMap);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, null);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response readBookings(boolean withConfirmedPayment) {
        try {
            List<Booking> bookingList = connDB.readBookings(withConfirmedPayment);
            if(bookingList == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, bookingList);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response readShows(HashMap<String, String> filtersMap) {
        try {
            List<Show> showList = connDB.readShows(filtersMap);
            if(showList == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, showList);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response selectShow(int showId) {
        try {
            Show show = connDB.selectShow(showId);
            if(show == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, show);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response readShowFreeSeats(int showId) {
        try {
            List<Seat> seatList = connDB.readShowFreeSeats(showId);
            if(seatList == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, seatList);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response readShowSeats(int showId) {
        try {
            List<Seat> seatList = connDB.readShowSeats(showId);
            if(seatList == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, seatList);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response selectSeat(String chosenRow, String chosenSeat, int showId) {
        try {
            Seat seat = connDB.selectSeat(chosenRow, chosenSeat, showId);
            if(seat == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, seat);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response confirmBooking(int showId, List<Seat> selectedSeats, int userId) {
        try {
            Booking booking = connDB.confirmBooking(showId, selectedSeats, userId);
            if(booking == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, booking);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response deleteBooking(int bookingId, int userId) {
        try {
            Booking booking = connDB.deleteBooking(bookingId, userId);
            if(booking == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, booking);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response payBooking(int selectedBooking, int userId) {
        try {
            Booking booking = connDB.payBooking(selectedBooking, userId);
            if(booking == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, booking);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }

    public Response makeShowVisible(int selectedShow) {
        try {
            connDB.makeShowVisible(selectedShow);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, null);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }
}
