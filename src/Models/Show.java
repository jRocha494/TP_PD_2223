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
    int visible;

    public Show(int id, String description, String type, String dateTime, int duration, String local, String place, String country, String ageRating, int visible) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.dateTime = dateTime;
        this.duration = duration;
        this.local = local;
        this.place = place;
        this.country = country;
        this.ageRating = ageRating;
        this.visible = visible;
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
                ", visible='" + visible + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }
}
