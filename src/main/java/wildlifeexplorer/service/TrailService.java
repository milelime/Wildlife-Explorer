package wildlifeexplorer.service;

import java.util.List;
import wildlifeexplorer.data.TrailList;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.model.Wildlife;

public class TrailService {
    private TrailList repository;

    public TrailService(TrailList repository) {
        this.repository = repository;
    }

    public void createTrail(String name, int id, double length, double elevation) {
        Trail trail = new Trail(name, id, length, elevation);
        repository.addTrail(trail);
        repository.saveFile();
    }

    public void editTrail(String name, int id, double length, double elevation) {
        Trail trail = repository.getTrailById(id);

        if (trail != null) {
            trail.setName(name);
            trail.setLength(length);
            trail.setElevation(elevation);
            repository.saveFile();
        } else {
            System.out.println("Trail not found");
        }
    }

    public void saveTrail() {
        repository.saveFile();
    }

    public void rateTrail(int id, double rating) {
        Trail trail = repository.getTrailById(id);

        if (trail != null) {
            trail.updateRating(rating);
            repository.saveFile();
        } else {
            System.out.println("Trail not found");
        }
    }

    public Trail getTrail(int id) {
        return repository.getTrailById(id);
    }

    public void addWildlife(int trailId, Wildlife wildlife) {
        Trail trail = repository.getTrailById(trailId);

        if (trail != null) {
            trail.addWildlife(wildlife);
            repository.saveFile();
        } else {
            System.out.println("Trail not found");
        }
    }

    public List<Trail> searchTrail(String name) {
        return repository.searchTrail(name);
    }

    public List<Trail> viewTrails() {
        return repository.viewTrail();
    }
}
