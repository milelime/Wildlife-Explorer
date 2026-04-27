package wildlifeexplorer.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import wildlifeexplorer.defaults.BundledDefaultsComparer;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.service.TrailService;

public class MainFrame extends JFrame {
    private final CardLayout layout;
    private final JPanel root;

    private final TrailService trailService;

    private final SearchPanel searchPanel;
    private final TrailPanel trailPanel;
    private final WildlifePanel wildlifePanel;

    public MainFrame(TrailService trailService) {
        super("Wildlife Explorer");
        this.trailService = trailService;

        this.layout = new CardLayout();
        this.root = new JPanel(layout);

        this.searchPanel = new SearchPanel(trailService, this::showTrail);
        this.trailPanel = new TrailPanel(trailService, this::showSearch, this::showWildlife);
        this.wildlifePanel = new WildlifePanel(trailService, this::showTrail);

        root.add(searchPanel, "search");
        root.add(trailPanel, "trail");
        root.add(wildlifePanel, "wildlife");

        setContentPane(root);
        setPreferredSize(new Dimension(900, 600));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showSearch();

        SwingUtilities.invokeLater(this::maybeOfferRemoveBundledSampleData);
    }

    /**
     * If {@code trails.json}/{@code wildlife.json} still match {@code defaults/*.json}, offer to clear them.
     * Once the user edits data or removes samples, this dialog is not shown again.
     */
    private void maybeOfferRemoveBundledSampleData() {
        if (!BundledDefaultsComparer.userMatchesBundledDefaults()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "This build includes sample trails and wildlife (from defaults/ in the project).\n\n"
                + "Remove the sample trails and wildlife and start with an empty list?\n"
                + "(Tracked files under defaults/ are not deleted; only your working copies are cleared.)",
            "Sample data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        trailService.clearAllUserData();
        searchPanel.refreshList();
        trailPanel.setTrail(null);
        wildlifePanel.setTrail(null);
        layout.show(root, "search");
    }

    private void showSearch() {
        searchPanel.refreshList();
        layout.show(root, "search");
    }

    private void showTrail(Trail trail) {
        trailPanel.setTrail(trail);
        layout.show(root, "trail");
    }

    private void showWildlife(Trail trail) {
        wildlifePanel.setTrail(trail);
        layout.show(root, "wildlife");
    }
}
