package db.valueObjects;

public class Post {

    public int id;
    public String username;
    public String text;
    public java.sql.Timestamp date; //TODO maby chage type
    public String name;
    public String email = null;
    public String phone = null;
    public String img;

    public Post() {
    }

    public Post(String userName, String text, java.sql.Timestamp date, String name, String email, String phone, String img) {
        this.username = userName;
        this.text = text;
        this.date = date;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.img = img;
    }

    public Post(int id, String userName, String text, java.sql.Timestamp date, String name, String email, String phone, String img) {
        this(userName, text, date, name, email, phone, img);
        this.id = id;
    }
}
