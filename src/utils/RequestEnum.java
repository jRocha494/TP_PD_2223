package utils;

import java.io.Serial;
import java.io.Serializable;

public enum RequestEnum implements Serializable {
    MSG_CONNECT_SERVER,
    MSG_UPDATE_DATABASE,
    REQUEST_AUTHENTICATE_USER,
    REQUEST_REGISTER_USER,
    REQUEST_EDIT_LOGIN_DATA,
    REQUEST_READ_BOOKINGS,
    REQUEST_READ_SHOWS,
    REQUEST_SELECT_SHOW,
    REQUEST_READ_SHOW_AVAILABLE_SEATS,
    REQUEST_SELECT_SEAT,
    REQUEST_CONFIRM_BOOKING,
    REQUEST_DELETE_BOOKING,
    REQUEST_PAY_BOOKING,
    REQUEST_MAKE_SHOW_VISIBLE;

    @Serial
    private static final long serialVersionUID = 1L;
}
