package db.valueObjects;

public class Piano {

    public static final String MAX_SIZE = "100";

    public int id;
    public boolean isUpright;
    public String model;
    public String manufacturer;
    public String country;
    public int size;
    public String color;
    public String finish;
    public int year;
    public boolean isNew;
    public String img;

    public Piano() {
    }

    public Piano(boolean isUpright, String model, String manufacturer, String country, int size, String color, String finish, int year, boolean isNew, String img) {
        this.isUpright = isUpright;
        this.model = model;
        this.manufacturer = manufacturer;
        this.country = country;
        this.size = size;
        this.color = color;
        this.finish = finish;
        this.year = year;
        this.isNew = isNew;
        this.img = img;
    }

    public Piano(int id, boolean isUpright, String model, String manufacturer, String country, int size, String color, String finish, int year, boolean isNew, String img) {
        this(isUpright, model, manufacturer, country, size, color, finish, year, isNew, img);
        this.id = id;
    }
}
