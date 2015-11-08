package db.valueObjects;

public class Bench {

    public int id;
    public String model;
    public String color;
    public String fabric;
    public boolean isAdjustable;
    public String img;

    public Bench() {
    }

    public Bench(String model, String color, String fabric, boolean isAdjustable, String img) {
        this.model = model;
        this.color = color;
        this.fabric = fabric;
        this.isAdjustable = isAdjustable;
        this.img = img;
    }

    public Bench(int id, String model, String color, String fabric, boolean isAdjustable, String img) {
        this(model, color, fabric, isAdjustable, img);
        this.id = id;
    }
}
