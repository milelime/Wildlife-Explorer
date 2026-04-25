import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Picture { 
    private String file;

    public Picture(String file){
        this.file = file;
    }

    public BufferedImage loadImage() throws IOException {
        return ImageIO.read(new File(file));
    }

    public String getFilePath() {
        return file;
    }
    
}
