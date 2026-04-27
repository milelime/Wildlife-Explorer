package wildlifeexplorer.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.service.TrailService;
import javax.swing.JPanel;

public class SearchPanel extends JPanel {
    private final TrailService trailService;
    private final Consumer<Trail> onOpenTrail;

    private final JTextField searchField;
    private final DefaultListModel<Trail> listModel;
    private final JList<Trail> trailList;

    public SearchPanel(TrailService trailService, Consumer<Trail> onOpenTrail) {
        super(new BorderLayout(12, 12));
        this.trailService = trailService;
        this.onOpenTrail = onOpenTrail;

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        this.searchField = new JTextField();
        top.add(searchField, BorderLayout.CENTER);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refreshList());
        top.add(searchBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        this.listModel = new DefaultListModel<>();
        this.trailList = new JList<>(listModel);
        trailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trailList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = value == null
                ? ""
                : value.getId() + " — " + value.getName() + " (rating " + String.format("%.2f", value.getRating()) + ")";
            return new JLabel(text) {{
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            }};
        });

        trailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && trailList.getSelectedValue() != null) {
                // no-op; enables open button conceptually
            }
        });

        add(new JScrollPane(trailList), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshList();
        });

        JButton newTrailBtn = new JButton("New Trail");
        newTrailBtn.addActionListener(e -> createTrailDialog());

        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(e -> {
            Trail selected = trailList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a trail first.");
                return;
            }
            onOpenTrail.accept(trailService.getTrail(selected.getId()));
        });

        bottom.add(refreshBtn);
        bottom.add(newTrailBtn);
        bottom.add(openBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim();
        List<Trail> trails = q.isEmpty() ? trailService.viewTrails() : trailService.searchTrail(q);

        trails.sort(Comparator.comparingInt(Trail::getId));

        listModel.clear();
        for (Trail t : trails) {
            listModel.addElement(t);
        }
    }

    private void createTrailDialog() {
        JTextField name = new JTextField();
        JTextField length = new JTextField();
        JTextField elevation = new JTextField();

        JPanel form = new JPanel(new BorderLayout(8, 8));
        JPanel grid = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        grid.add(new JLabel("Name"));
        grid.add(name);
        grid.add(new JLabel("Length"));
        grid.add(length);
        grid.add(new JLabel("Elevation"));
        grid.add(elevation);
        form.add(grid, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, form, "Create Trail", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String n = name.getText().trim();
            double len = Double.parseDouble(length.getText().trim());
            double elev = Double.parseDouble(elevation.getText().trim());
            int id = nextTrailId();

            trailService.createTrail(n, id, len, elev);
            refreshList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create trail: " + ex.getMessage());
        }
    }

    private int nextTrailId() {
        int max = 0;
        for (Trail t : trailService.viewTrails()) {
            if (t.getId() > max) {
                max = t.getId();
            }
        }
        return max + 1;
    }
}
