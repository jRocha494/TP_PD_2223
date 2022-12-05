package Data;

import java.util.ArrayList;
import java.util.List;

public class ClientSelectionData {
    Show selectedShow;
    List<Seat> selectedSeats;
    Booking booking;

    public ClientSelectionData() {
        selectedShow = null;
        selectedSeats = new ArrayList<>();
        booking = null;
    }

    public Show getSelectedShow() {
        return selectedShow;
    }

    public void setSelectedShow(Show selectedShow) {
        this.selectedShow = selectedShow;
    }

    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }

    public void addSeat(Seat newSeat) {
        selectedSeats.add(newSeat);
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
