package wildlifeexplorer.model;

import java.util.ArrayList;
import java.util.List;

public class Wildlife {
    private String name;
    private int id;
    private String description;
    private String notes;
    private List<String> pictures;

    public Wildlife(String name, int id, String description, String notes) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.notes = notes;
        this.pictures = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void addPictures(String picture) {
        pictures.add(picture);
    }
}
