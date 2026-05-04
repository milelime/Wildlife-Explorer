package wildlifeexplorer.defaults;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import wildlifeexplorer.AppPaths;
import wildlifeexplorer.data.Database;

/**
 * Detects whether the user's {@code trails.json} / {@code wildlife.json} still match the
 * bundled files under {@code defaults/} (semantic compare; ignores root {@code updatedAt}).
 */
public final class BundledDefaultsComparer {

    private BundledDefaultsComparer() {}

    public static boolean userMatchesBundledDefaults() {
        try {
            Path ut = AppPaths.userTrailsJson();
            Path uw = AppPaths.userWildlifeJson();

            if (!Files.exists(ut) || !Files.exists(uw)) {
                return false;
            }

            String userTrailsJson = Files.readString(ut, StandardCharsets.UTF_8);
            String userWildJson = Files.readString(uw, StandardCharsets.UTF_8);
            String defTrailsJson = DefaultBundle.bundledTrailsJsonForCompare();
            String defWildJson = DefaultBundle.bundledWildlifeJsonForCompare();

            Map<?, ?> userTrRoot = asMap(Database.parseJsonDocument(userTrailsJson.trim()));
            Map<?, ?> defTrRoot = asMap(Database.parseJsonDocument(defTrailsJson.trim()));
            Map<?, ?> userWRoot = asMap(Database.parseJsonDocument(userWildJson.trim()));
            Map<?, ?> defWRoot = asMap(Database.parseJsonDocument(defWildJson.trim()));

            if (userTrRoot == null || defTrRoot == null || userWRoot == null || defWRoot == null) {
                return false;
            }

            Object userTrails = userTrRoot.get("trails");
            Object defTrails = defTrRoot.get("trails");
            Object userWildlife = userWRoot.get("wildlife");
            Object defWildlife = defWRoot.get("wildlife");

            return deepEqual(userTrails, defTrails) && deepEqual(userWildlife, defWildlife);
        } catch (Exception e) {
            return false;
        }
    }

    private static Map<?, ?> asMap(Object o) {
        return o instanceof Map ? (Map<?, ?>) o : null;
    }

    private static boolean deepEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            if (Double.isNaN(da) && Double.isNaN(db)) {
                return true;
            }
            return Math.abs(da - db) <= 1e-9 * Math.max(1.0, Math.max(Math.abs(da), Math.abs(db)));
        }
        if (a instanceof Map && b instanceof Map) {
            Map<?, ?> ma = (Map<?, ?>) a;
            Map<?, ?> mb = (Map<?, ?>) b;
            if (ma.size() != mb.size()) {
                return false;
            }
            for (Object k : ma.keySet()) {
                if (!mb.containsKey(k)) {
                    return false;
                }
                if (!deepEqual(ma.get(k), mb.get(k))) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof List && b instanceof List) {
            List<?> la = (List<?>) a;
            List<?> lb = (List<?>) b;
            if (la.size() != lb.size()) {
                return false;
            }
            for (int i = 0; i < la.size(); i++) {
                if (!deepEqual(la.get(i), lb.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return Objects.equals(a, b);
    }
}
