import java.util.ArrayList;
import java.util.List;

public class TrailList {
    private List<Trail> trails;
    private String file;    
    
    public TrailList(String file){
        this.trails = new ArrayList<>();
        this.file = file;
    }

    public void addTrail( Trail t){
        trails.add(t);
    }

    public Trail getTrail(int id){
        for (Trail trail: trails){
            if (trail.getId() == id){
                return trail;
            }
        }
        System.out.println("Trail not found");
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
        System.out.println("Loading file " + file);
    }

    public void saveFile() {
        System.out.println("Saving file " + file);
    }
}
