package wildlifeexplorer.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import wildlifeexplorer.AppPaths;
import wildlifeexplorer.io.PreviewImageWriter;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.model.Wildlife;
import wildlifeexplorer.service.TrailService;

public class WildlifePanel extends JPanel {
    private final TrailService trailService;
    private final Consumer<Trail> onDone;

    private Trail trail;

    private final JTextField name;
    private final JTextField description;
    private final JTextField notes;
    private final JTextField picture;
    private final JButton chooseImageBtn;

    public WildlifePanel(TrailService trailService, Consumer<Trail> onDone) {
        super(new BorderLayout(12, 12));
        this.trailService = trailService;
        this.onDone = onDone;

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        this.name = new JTextField();
        this.description = new JTextField();
        this.notes = new JTextField();
        this.picture = new JTextField();

        form.add(new JLabel("Wildlife name"));
        form.add(name);
        form.add(new JLabel("Description"));
        form.add(description);
        form.add(new JLabel("Notes"));
        form.add(notes);
        form.add(new JLabel("Picture path (optional)"));
        form.add(picture);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.chooseImageBtn = new JButton("Choose Image…");
        chooseImageBtn.addActionListener(e -> chooseImage());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            if (trail != null) onDone.accept(trailService.getTrail(trail.getId()));
        });
        JButton save = new JButton("Save");
        save.addActionListener(e -> save());

        bottom.add(chooseImageBtn);
        bottom.add(cancel);
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setTrail(Trail trail) {
        this.trail = trail == null ? null : trailService.getTrail(trail.getId());
        clearForm();
    }

    private void clearForm() {
        name.setText("");
        description.setText("");
        notes.setText("");
        picture.setText("");
    }

    private void save() {
        if (trail == null) return;
        try {
            Wildlife w = new Wildlife(
                name.getText().trim(),
                0,
                description.getText().trim(),
                notes.getText().trim()
            );
            String pic = picture.getText().trim();
            if (!pic.isEmpty()) {
                Path rp = AppPaths.resolveDataPath(pic);
                if (rp == null) {
                    throw new IllegalArgumentException("Invalid picture path");
                }
                validateImageFile(rp.toFile());
                w.addPictures(pic);
            }
            trailService.addWildlife(trail.getId(), w);
            onDone.accept(trailService.getTrail(trail.getId()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not add wildlife: " + ex.getMessage());
        }
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Wildlife Image");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
            "Images (png, jpg, jpeg, gif, bmp, webp)",
            "png", "jpg", "jpeg", "gif", "bmp", "webp"
        ));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File f = chooser.getSelectedFile();
        if (f == null) {
            return;
        }

        try {
            validateImageFile(f);
            Files.createDirectories(AppPaths.userWildlifeImagesDir());
            Path dest = AppPaths.userWildlifeImagesDir().resolve(
                "wildlife-" + System.currentTimeMillis() + "-" + PreviewImageWriter.stemForPreviewFile(f.getName())
                    + ".jpg");
            PreviewImageWriter.writePreviewCopy(f, dest);
            picture.setText(AppPaths.toStoredPath(dest));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "That file is not a valid image: " + ex.getMessage());
        }
    }

    private void validateImageFile(File f) throws Exception {
        if (!f.exists() || !f.isFile()) {
            throw new IllegalArgumentException("File does not exist");
        }
        BufferedImage img = ImageIO.read(f);
        if (img == null) {
            throw new IllegalArgumentException("Unsupported/invalid image format");
        }

        String lower = f.getName().toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".png")
            || lower.endsWith(".jpg")
            || lower.endsWith(".jpeg")
            || lower.endsWith(".gif")
            || lower.endsWith(".bmp")
            || lower.endsWith(".webp"))) {
            // Extension doesn't match common types, but ImageIO accepted it; allow.
        }
    }
}
