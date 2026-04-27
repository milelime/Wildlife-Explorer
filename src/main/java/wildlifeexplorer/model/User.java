package wildlifeexplorer.model;

public class User {
    private String name;
    private int id;

    public User(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean validateUser(String name, int id) {
        return this.name.equals(name) && this.id == id;
    }
}

