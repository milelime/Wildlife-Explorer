import java.util.ArrayList;
import java.util.List;

public class Trail {
    private String name;
    private int id;
    private double length;
    private double elevation;
    private double rating;
    private List<Picture> pictures;
    private List<Wildlife> wildlifeList;
    //to get the average rate 
    private double totalRating; 
    private int numRating;


public Trail(String name, int id, double length, double elevation) {
    this.name = name;
    this.id = id;
    this.length = length;
    this.elevation = elevation;
    this.rating = 0.0;
    this.pictures = new ArrayList<>();
    this.wildlifeList = new ArrayList<>();
    this.totalRating = 0.0;
    this.numRating = 0;
}

public String getName() {
        return name;
}

public int getId() {
        return id;
}

public double getLength() {
    return length;
}

public double getElevation() {
    return elevation;
}

public double getRating(){
    return rating;
}

public List<Picture> getPictures(){
    return pictures;
}

public List<Wildlife> getWildlifeList() {
    return wildlifeList;
}

public void setName(String name){
    this.name = name;
}

public void setLength(double length){
    this.length = length;
}

public void setElevation(double elevation){
    this.elevation = elevation;
}

public void addPicture(Picture picture){
    pictures.add(picture);
}

public void updateRating(double newRating) { 
    totalRating += newRating;
    numRating++;
    rating = totalRating / numRating;
}

public void addWildlife(Wildlife wildlife){
    wildlifeList.add(wildlife);
}

}