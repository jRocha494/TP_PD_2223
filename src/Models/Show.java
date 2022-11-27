package Models;

import java.util.List;

public class Show {
    int id;
    String description;
    String type;
    String dateTime;
    int duration;
    String local;
    String place;
    String country;
    String ageRating;

    public Show(int id, String description, String type, String dateTime, int duration, String local, String place, String country, String ageRating) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.dateTime = dateTime;
        this.duration = duration;
        this.local = local;
        this.place = place;
        this.country = country;
        this.ageRating = ageRating;
    }

    @Override
    public String toString() {
        return "Show{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", duration=" + duration +
                ", local='" + local + '\'' +
                ", place='" + place + '\'' +
                ", country='" + country + '\'' +
                ", ageRating='" + ageRating + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }
}
