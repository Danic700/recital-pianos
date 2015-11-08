package db.valueObjects;

public class User {

    public String username;
    public String password;

    public User() {
    }

    public User(String email, String password) {
        this.username = email;
        this.password = password;
    }
}
