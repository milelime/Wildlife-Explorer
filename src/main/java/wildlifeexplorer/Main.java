package wildlifeexplorer;

import javax.swing.SwingUtilities;
import wildlifeexplorer.data.Database;
import wildlifeexplorer.service.TrailService;
import wildlifeexplorer.ui.MainFrame;

public class Main {

    public static void main(String[] args) {
        try {
            AppPaths.bootstrapUserDataFromDefaultsIfMissing();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize data files from defaults/", e);
        }

        Database database = new Database(
            AppPaths.userTrailsJson().toString(),
            AppPaths.userWildlifeJson().toString());
        TrailService service = new TrailService(database);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
    
}
