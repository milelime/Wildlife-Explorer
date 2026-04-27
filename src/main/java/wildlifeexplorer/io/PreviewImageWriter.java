package wildlifeexplorer.io;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Writes a local copy of an image scaled to preview dimensions (never upscaled) and re-encoded as JPEG.
 */
public final class PreviewImageWriter {

    /** Matches trail detail preview pane max size. */
    public static final int PREVIEW_MAX_WIDTH = 380;
    public static final int PREVIEW_MAX_HEIGHT = 260;

    /** Safe basename without extension for {@code *.jpg} output filenames. */
    public static String stemForPreviewFile(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "photo";
        }
        String base = Path.of(originalFileName).getFileName().toString()
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        int dot = base.lastIndexOf('.');
        return dot > 0 ? base.substring(0, dot) : base;
    }

    private static final float JPEG_QUALITY = 0.82f;

    private PreviewImageWriter() {}

    /**
     * Reads {@code source}, scales down if wider/taller than max dimensions, converts to RGB, writes JPEG.
     */
    public static void writePreviewCopy(File source, Path destination) throws IOException {
        BufferedImage src = ImageIO.read(source);
        if (src == null) {
            throw new IOException("Unsupported or invalid image");
        }
        BufferedImage scaled = scaleDownPreserveAspect(src, PREVIEW_MAX_WIDTH, PREVIEW_MAX_HEIGHT);
        BufferedImage rgb = toRgb(scaled);
        Files.createDirectories(destination.getParent());
        writeJpeg(rgb, destination, JPEG_QUALITY);
    }

    static BufferedImage scaleDownPreserveAspect(BufferedImage src, int maxW, int maxH) {
        int w = src.getWidth();
        int h = src.getHeight();
        double scale = Math.min((double) maxW / w, (double) maxH / h);
        if (scale >= 1.0) {
            return src;
        }
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    private static BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private static void writeJpeg(BufferedImage img, Path dest, float quality) throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        if (!iter.hasNext()) {
            throw new IOException("No JPEG ImageWriter available");
        }
        ImageWriter writer = iter.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(Files.newOutputStream(dest))) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
