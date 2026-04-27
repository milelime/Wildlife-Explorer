package wildlifeexplorer;

import wildlifeexplorer.data.TrailList;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.service.TrailService;

public class Main {

    public static void main(String[] args) {
        TrailList trailList = new TrailList("test.txt");
        TrailService service = new TrailService(trailList);

        service.createTrail("route 1",1, 22, 10);
        service.rateTrail(1, 7);
        service.rateTrail(1, 1);
        Trail t = service.getTrail(1);
        System.out.println(t.getName());
        System.out.println(t.getRating());
    }
    
}
