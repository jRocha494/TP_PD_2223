package Data;

public class User {
    int id;
    String name;
    String username;
    String password;
    int isAuthenticated;
    int isAdministrator;

    public User(int id, String username, String name, String password, int isAdministrator, int isAuthenticated) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.isAuthenticated = isAuthenticated;
        this.isAdministrator = isAdministrator;
    }

    public User(String username, String name, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int IsAuthenticated() {
        return isAuthenticated;
    }

    public int IsAdministrator() {
        return isAdministrator;
    }
}
