package Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Booking implements Serializable {
    @Serial
    static final long serialVersionUID = 1L;
    int id;
    String dateTime;
    int paid;
    int idUser;
    int idEspetaculo;
    List<Seat> seatList;

    public Booking(int id, String dateTime, int paid, int idUser, int idEspetaculo) {
        this.id = id;
        this.dateTime = dateTime;
        this.paid = paid;
        this.idUser = idUser;
        this.idEspetaculo = idEspetaculo;
        seatList = new ArrayList<>();
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", dateTime='" + dateTime + '\'' +
                ", paid=" + paid +
                ", idUser=" + idUser +
                ", idEspetaculo=" + idEspetaculo +
                ", seatList=" + seatList +
                '}';
    }

    public static String fullDetails(Show show, List<Seat> seatList, boolean withPrice){
        float finalPrice = 0;
        String info = "Booking information:\n\tShow: " + show.toString() + "\n\tSelected Seats:\n";

        Collections.sort(seatList);
        for(Seat seat : seatList){
            info += "\t\t" + seat.toString() + "\n";
            finalPrice += seat.getPrice();
        }

        if(withPrice)
            info += "\tFinal Price: " + finalPrice + "\n";

        return info;
    }
}
