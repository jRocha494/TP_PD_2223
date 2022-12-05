package Data;

import java.io.Serial;
import java.io.Serializable;

public class Seat implements Comparable<Seat>, Serializable {
    @Serial
    static final long serialVersionUID = 1L;
    int id;
    String row;
    String seat;
    float price;
    int showId;

    public Seat(int id, String row, String seat, float price, int showId) {
        this.id = id;
        this.row = row;
        this.seat = seat;
        this.price = price;
        this.showId = showId;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", row='" + row + '\'' +
                ", seat='" + seat + '\'' +
                ", price=" + price +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getRow() {
        return row;
    }

    public String getSeat() {
        return seat;
    }

    public float getPrice() {
        return price;
    }

    @Override
    public int compareTo(Seat o) {
        if (getRow() == null || o.getSeat() == null) {
            return 0;
        }
        if(getRow().equals(o.getRow()))
            return getSeat().compareTo(o.getSeat());
        return getRow().compareTo(o.getRow());
    }
}
