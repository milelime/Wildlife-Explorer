package wildlifeexplorer.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import wildlifeexplorer.data.Database;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.model.Wildlife;

public class TrailService {
    private Database database;

    public TrailService(Database database) {
        this.database = database;
    }

    public void createTrail(String name, int id, double length, double elevation) {
        Trail trail = new Trail(name, id, length, elevation);
        database.insertTrail(trail);
    }

    public void editTrail(String name, int id, double length, double elevation) {
        Trail trail = database.fetchTrailById(id);

        if (trail != null) {
            trail.setName(name);
            trail.setLength(length);
            trail.setElevation(elevation);
            database.save();
        } else {
            System.out.println("Trail not found");
        }
    }

    //remove trail
    public void deleteTrail(int id) {
        database.deleteTrail(id);
    }

    public void saveTrail() {
        database.save();
    }

    public void rateTrail(int id, double rating) {
        database.insertRating(id, rating);
    }

    public Trail getTrail(int id) {
        return database.fetchTrailById(id);
    }

    public void addWildlife(int trailId, Wildlife wildlife) {
        database.insertWildlife(trailId, wildlife);
    }

    /** All species in the global catalog, sorted by id (for pickers). */
    public List<Wildlife> listAllWildlife() {
        List<Wildlife> list = new ArrayList<>(database.getAllWildlife());
        list.sort(Comparator.comparingInt(Wildlife::getId));
        return list;
    }

    /** Attach a catalog wildlife record to a trail without creating a duplicate species entry. */
    public void attachExistingWildlifeToTrail(int trailId, int wildlifeId) {
        database.attachExistingWildlifeToTrail(trailId, wildlifeId);
    }

    /** Detach a species from this trail; it remains in the catalog for other trails. */
    public void removeWildlifeFromTrail(int trailId, int wildlifeId) {
        database.removeWildlifeFromTrail(trailId, wildlifeId);
    }

    public List<Trail> searchTrail(String name) {
        return database.findTrails(name);
    }

    public List<Trail> viewTrails() {
        return database.getAllTrails();
    }

    /** Clears all persisted trails and wildlife (does not touch tracked files under {@code defaults/}). */
    public void clearAllUserData() {
        database.clearAllUserData();
    }
}
