package wildlifeexplorer.data;

import java.util.ArrayList;
import java.util.List;
import wildlifeexplorer.model.Trail;

public class TrailList {
    private List<Trail> trails;
    private String fileName;

    public TrailList(String fileName) {
        this.trails = new ArrayList<>();
        this.fileName = fileName;
    }

    public void addTrail(Trail t) {
        trails.add(t);
    }

    public Trail getTrailById(int id) {
        for (Trail trail : trails) {
            if (trail.getId() == id) {
                return trail;
            }
        }
        return null;
    }

    public List<Trail> searchTrail(String name) {
        List<Trail> result = new ArrayList<>();
        for (Trail trail : trails) {
            if (trail.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(trail);
            }
        }
        return result;
    }

    public List<Trail> viewTrail() {
        return trails;
    }

    public void loadFile() {
        System.out.println("Loading file " + fileName);
    }

    public void saveFile() {
        System.out.println("Saving file " + fileName);
    }
}

