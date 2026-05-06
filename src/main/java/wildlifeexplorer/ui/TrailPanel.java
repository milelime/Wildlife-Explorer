package wildlifeexplorer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import wildlifeexplorer.AppPaths;
import wildlifeexplorer.io.PreviewImageWriter;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.model.Wildlife;
import wildlifeexplorer.service.TrailService;

public class TrailPanel extends JPanel {
    private final TrailService trailService;
    private final Runnable onBack;
    private final Consumer<Trail> onAddWildlife;

    private Trail currentTrail;

    private final JLabel title;
    private final JLabel meta;
    private final JPanel wildlifeListPanel;
    private final JPanel trailPicturesPanel;

    private static final int PREVIEW_MAX_WIDTH = 380;
    private static final int PREVIEW_MAX_HEIGHT = 260;
    private static final int WILDLIFE_THUMB_MAX_WIDTH = 220;
    private static final int WILDLIFE_THUMB_MAX_HEIGHT = 140;

    public TrailPanel(TrailService trailService, Runnable onBack, Consumer<Trail> onAddWildlife) {
        super(new BorderLayout(12, 12));
        this.trailService = trailService;
        this.onBack = onBack;
        this.onAddWildlife = onAddWildlife;

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        this.title = new JLabel("Trail");
        title.setFont(title.getFont().deriveFont(18f));
        this.meta = new JLabel("");
        top.add(title, BorderLayout.NORTH);
        top.add(meta, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        this.wildlifeListPanel = new JPanel();
        wildlifeListPanel.setLayout(new BoxLayout(wildlifeListPanel, BoxLayout.Y_AXIS));
        wildlifeListPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane wildlifeScroll = new JScrollPane(wildlifeListPanel);
        wildlifeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        wildlifeScroll.setBorder(new TitledBorder("Wildlife sightings"));

        this.trailPicturesPanel = new JPanel();
        trailPicturesPanel.setLayout(new BoxLayout(trailPicturesPanel, BoxLayout.Y_AXIS));
        trailPicturesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane picsScroll = new JScrollPane(trailPicturesPanel);
        picsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        picsScroll.setBorder(new TitledBorder("Trail images"));

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.add(wildlifeScroll);
        center.add(picsScroll);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> onBack.run());

        JButton addPicBtn = new JButton("Add Trail Image…");
        addPicBtn.addActionListener(e -> addTrailImage());

        JButton rateBtn = new JButton("Rate");
        rateBtn.addActionListener(e -> rateDialog());

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editDialog());

        JButton addWildlifeBtn = new JButton("Add Wildlife…");
        addWildlifeBtn.addActionListener(e -> addWildlifeChoice());

        bottom.add(backBtn);
        bottom.add(editBtn);
        bottom.add(rateBtn);
        bottom.add(addWildlifeBtn);
        bottom.add(addPicBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    public void setTrail(Trail trail) {
        this.currentTrail = trail == null ? null : trailService.getTrail(trail.getId());
        refresh();
    }

    private void refresh() {
        if (currentTrail == null) {
            title.setText("Trail");
            meta.setText("");
            rebuildWildlifePane();
            rebuildTrailPicturesPane();
            return;
        }

        title.setText(currentTrail.getId() + " — " + currentTrail.getName());
        meta.setText(
            "Length: " + currentTrail.getLength() + " miles"
                + " | Elevation: " + currentTrail.getElevation() + " ft"
                + " | Rating: " + String.format("%.2f", currentTrail.getRating())
        );

        rebuildWildlifePane();
        rebuildTrailPicturesPane();
    }

    private void rebuildWildlifePane() {
        wildlifeListPanel.removeAll();

        if (currentTrail == null || currentTrail.getWildlifeList().isEmpty()) {
            JLabel empty = new JLabel("(none yet)", SwingConstants.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            wildlifeListPanel.add(empty);
            wildlifeListPanel.revalidate();
            wildlifeListPanel.repaint();
            return;
        }

        for (Wildlife w : currentTrail.getWildlifeList()) {
            JPanel block = new JPanel(new BorderLayout(0, 8));
            block.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));

            JTextArea text = new JTextArea();
            text.setEditable(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setOpaque(false);
            text.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            StringBuilder sb = new StringBuilder();
            sb.append(w.getId()).append(" — ").append(w.getName()).append("\n");
            sb.append(w.getDescription()).append("\n");
            sb.append("Notes: ").append(w.getNotes() == null || w.getNotes().isEmpty() ? "—" : w.getNotes());
            text.setText(sb.toString());

            JButton removeBtn = new JButton("Remove");
            removeBtn.addActionListener(e -> confirmRemoveWildlifeFromTrail(w));

            JPanel headerRow = new JPanel(new BorderLayout(8, 0));
            headerRow.setOpaque(false);
            headerRow.add(text, BorderLayout.CENTER);
            headerRow.add(removeBtn, BorderLayout.EAST);

            JPanel thumbs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            List<String> pics = w.getPictures();
            if (pics == null || pics.isEmpty()) {
                thumbs.add(new JLabel("(no photos)", SwingConstants.LEFT));
            } else {
                for (String stored : pics) {
                    if (stored == null || stored.isBlank()) {
                        continue;
                    }
                    Path resolved = AppPaths.resolveDataPath(stored.trim());
                    if (resolved == null) {
                        continue;
                    }
                    File imgFile = resolved.toFile();
                    try {
                        BufferedImage bi = ImageIO.read(imgFile);
                        if (bi == null) {
                            thumbs.add(new JLabel("[bad image]"));
                        } else {
                            ImageIcon icon = new ImageIcon(scaleImage(bi, WILDLIFE_THUMB_MAX_WIDTH, WILDLIFE_THUMB_MAX_HEIGHT));
                            JLabel thumb = new JLabel(icon);
                            thumb.setToolTipText(stored.trim());
                            thumbs.add(thumb);
                        }
                    } catch (Exception ex) {
                        thumbs.add(new JLabel("[error loading]"));
                    }
                }
            }

            block.add(headerRow, BorderLayout.NORTH);
            block.add(thumbs, BorderLayout.SOUTH);
            wildlifeListPanel.add(block);
            wildlifeListPanel.add(Box.createVerticalStrut(8));
        }

        wildlifeListPanel.revalidate();
        wildlifeListPanel.repaint();
    }

    private void confirmRemoveWildlifeFromTrail(Wildlife w) {
        if (currentTrail == null) {
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
            this,
            "Remove \"" + w.getName() + "\" from this trail?\n"
                + "The species will stay in your catalog for other trails.",
            "Remove wildlife",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            trailService.removeWildlifeFromTrail(currentTrail.getId(), w.getId());
            currentTrail = trailService.getTrail(currentTrail.getId());
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not remove wildlife: " + ex.getMessage());
        }
    }

    private void addWildlifeChoice() {
        if (currentTrail == null) {
            return;
        }

        String[] options = {"Choose from list", "Create new animal", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "How would you like to add wildlife to this trail?",
            "Add wildlife",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            attachExistingWildlifeDialog();
        } else if (choice == 1) {
            onAddWildlife.accept(currentTrail);
        }
    }

    private void attachExistingWildlifeDialog() {
        if (currentTrail == null) {
            return;
        }

        List<Wildlife> catalog = trailService.listAllWildlife();
        if (catalog.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "There are no wildlife records in your catalog yet.\nUse \"New Wildlife…\" to create one first.",
                "Attach wildlife",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Set<Integer> onTrail = new HashSet<>();
        for (Wildlife w : currentTrail.getWildlifeList()) {
            onTrail.add(w.getId());
        }

        List<Wildlife> available = new ArrayList<>();
        for (Wildlife w : catalog) {
            if (!onTrail.contains(w.getId())) {
                available.add(w);
            }
        }

        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Every wildlife species in your catalog is already linked to this trail.",
                "Attach wildlife",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Wildlife[] arr = available.toArray(new Wildlife[0]);
        JList<Wildlife> list = new JList<>(arr);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(Math.min(12, arr.length));
        list.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
            JLabel lab = new JLabel(value.getId() + " — " + value.getName());
            lab.setOpaque(true);
            lab.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (isSelected) {
                lab.setBackground(list1.getSelectionBackground());
                lab.setForeground(list1.getSelectionForeground());
            } else {
                lab.setBackground(list1.getBackground());
                lab.setForeground(list1.getForeground());
            }
            return lab;
        });

        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(420, 280));

        int ok = JOptionPane.showConfirmDialog(
            this,
            sp,
            "Attach existing wildlife",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        Wildlife sel = list.getSelectedValue();
        if (sel == null) {
            return;
        }

        try {
            trailService.attachExistingWildlifeToTrail(currentTrail.getId(), sel.getId());
            currentTrail = trailService.getTrail(currentTrail.getId());
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not attach wildlife: " + ex.getMessage());
        }
    }

    private void rebuildTrailPicturesPane() {
        trailPicturesPanel.removeAll();

        if (currentTrail == null || currentTrail.getPictures().isEmpty()) {
            JLabel empty = new JLabel("(none yet)", SwingConstants.CENTER);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            trailPicturesPanel.add(empty);
            trailPicturesPanel.revalidate();
            trailPicturesPanel.repaint();
            return;
        }

        for (String stored : currentTrail.getPictures()) {
            if (stored == null || stored.isBlank()) {
                continue;
            }
            Path resolved = AppPaths.resolveDataPath(stored.trim());
            if (resolved == null) {
                continue;
            }
            File f = resolved.toFile();
            JPanel row = new JPanel(new BorderLayout(0, 6));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, PREVIEW_MAX_HEIGHT + 40));

            try {
                BufferedImage bi = ImageIO.read(f);
                if (bi == null) {
                    row.add(new JLabel("Could not load: " + f.getName()), BorderLayout.CENTER);
                } else {
                    ImageIcon icon = new ImageIcon(scaleImage(bi, PREVIEW_MAX_WIDTH, PREVIEW_MAX_HEIGHT));
                    JLabel imgLabel = new JLabel(icon);
                    imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    row.add(imgLabel, BorderLayout.CENTER);
                }
            } catch (Exception ex) {
                row.add(new JLabel("Error: " + f.getName() + " — " + ex.getMessage()), BorderLayout.CENTER);
            }

            JLabel pathLabel = new JLabel(stored.trim());
            pathLabel.setFont(pathLabel.getFont().deriveFont(11f));
            row.add(pathLabel, BorderLayout.SOUTH);

            trailPicturesPanel.add(row);
            trailPicturesPanel.add(Box.createVerticalStrut(12));
        }

        trailPicturesPanel.revalidate();
        trailPicturesPanel.repaint();
    }

    private static Image scaleImage(BufferedImage src, int maxW, int maxH) {
        int w = src.getWidth();
        int h = src.getHeight();
        double scale = Math.min((double) maxW / w, (double) maxH / h);
        if (scale > 1.0) {
            scale = 1.0;
        }
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        return src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
    }

    private void addTrailImage() {
        if (currentTrail == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Trail Image");
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
            Files.createDirectories(AppPaths.userTrailImagesDir());
            Path dest = AppPaths.userTrailImagesDir().resolve(
                "trail-" + currentTrail.getId() + "-" + System.currentTimeMillis() + "-"
                    + PreviewImageWriter.stemForPreviewFile(f.getName()) + ".jpg");
            PreviewImageWriter.writePreviewCopy(f, dest);
            currentTrail.addPicture(AppPaths.toStoredPath(dest));
            trailService.saveTrail();
            currentTrail = trailService.getTrail(currentTrail.getId());
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "That file is not a valid image: " + ex.getMessage());
        }
    }

    private void rateDialog() {
        if (currentTrail == null) {
            return;
        }

        Window owner = SwingUtilities.windowForComponent(this);
        JDialog dlg = new JDialog(owner, "Rate trail", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel grid = new JPanel(new GridLayout(2, 6, 8, 8));
        for (int i = 0; i <= 10; i++) {
            final int score = i;
            JButton b = new JButton(Integer.toString(i));
            b.setFocusable(true);
            b.addActionListener(e -> {
                try {
                    trailService.rateTrail(currentTrail.getId(), score);
                    currentTrail = trailService.getTrail(currentTrail.getId());
                    refresh();
                    dlg.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Could not rate trail: " + ex.getMessage());
                }
            });
            grid.add(b);
        }
        grid.add(new JPanel());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());
        south.add(cancel);

        dlg.setLayout(new BorderLayout(8, 8));
        dlg.add(new JLabel("Choose a rating from 0 to 10:", SwingConstants.LEADING), BorderLayout.NORTH);
        dlg.add(grid, BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
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

    private void editDialog() {
        if (currentTrail == null) return;

        JTextField name = new JTextField(currentTrail.getName(), 8);
        JTextField length = new JTextField(String.valueOf(currentTrail.getLength()), 8);
        JTextField elevation = new JTextField(String.valueOf(currentTrail.getElevation()), 8);

        JPanel form = new JPanel(new BorderLayout(8, 8));
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.add(new JLabel("Name"));
        grid.add(name);
        grid.add(new JLabel("Length"));
        JPanel lengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        lengthPanel.add(length);
        lengthPanel.add(new JLabel("miles"));
        grid.add(lengthPanel);
        //grid.add(length);
       // grid.add(new JLabel("Elevation"));
        //grid.add(elevation);
        grid.add(new JLabel("Elevation:"));
        JPanel elevationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
        elevationPanel.add(elevation);
        elevationPanel.add(new JLabel("ft"));
        grid.add(elevationPanel);
        form.add(grid, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit Trail", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            String n = name.getText().trim();
            double len = Double.parseDouble(length.getText().trim());
            double elev = Double.parseDouble(elevation.getText().trim());
            trailService.editTrail(n, currentTrail.getId(), len, elev);
            currentTrail = trailService.getTrail(currentTrail.getId());
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not edit trail: " + ex.getMessage());
        }
    }
}
