import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** One-off helper: generates placeholder PNGs under defaults/images/. Run from repo root:
 * {@code javac tools/GenerateDefaultImages.java && java -cp tools GenerateDefaultImages}
 */
public class GenerateDefaultImages {

    private static void write(File out, Color bg, Color fg, String title, String subtitle) throws Exception {
        BufferedImage img = new BufferedImage(420, 260, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillRect(0, 0, 420, 260);
        g.setColor(fg);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.drawString(title, 28, 110);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g.drawString(subtitle, 28, 148);
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    public static void main(String[] args) throws Exception {
        File base = new File("defaults/images");
        File trails = new File(base, "trails");
        File wildlife = new File(base, "wildlife");
        trails.mkdirs();
        wildlife.mkdirs();

        write(new File(trails, "woodland-loop.png"),
            new Color(34, 110, 60),
            Color.WHITE,
            "Woodland Loop",
            "Default trail photo (placeholder)");

        write(new File(trails, "ridge-overlook.png"),
            new Color(55, 118, 168),
            Color.WHITE,
            "Ridge Overlook",
            "Default trail photo (placeholder)");

        write(new File(wildlife, "white-tailed-deer.png"),
            new Color(139, 95, 55),
            Color.WHITE,
            "White-tailed Deer",
            "Default wildlife photo (placeholder)");

        write(new File(wildlife, "red-fox.png"),
            new Color(175, 75, 55),
            Color.WHITE,
            "Red Fox",
            "Default wildlife photo (placeholder)");

        write(new File(wildlife, "pileated-woodpecker.png"),
            new Color(35, 35, 35),
            Color.WHITE,
            "Pileated Woodpecker",
            "Default wildlife photo (placeholder)");

        System.out.println("Wrote PNGs under defaults/images/");
    }
}
