package wildlifeexplorer;

import java.io.IOException;
import java.nio.file.Path;

import wildlifeexplorer.defaults.DefaultBundle;

/**
 * Project root is {@code user.dir} unless {@code -Dwildlifeexplorer.root=/path} is set.
 * Image paths stored in JSON are relative to that root (or absolute for legacy data).
 */
public final class AppPaths {

    private static final Path ROOT;

    static {
        String prop = System.getProperty("wildlifeexplorer.root");
        if (prop != null && !prop.isBlank()) {
            ROOT = Path.of(prop).toAbsolutePath().normalize();
        } else {
            ROOT = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        }
    }

    private AppPaths() {}

    public static Path root() {
        return ROOT;
    }

    public static Path userTrailsJson() {
        return ROOT.resolve("trails.json");
    }

    public static Path userWildlifeJson() {
        return ROOT.resolve("wildlife.json");
    }

    public static Path defaultsTrailsJson() {
        return ROOT.resolve("defaults/trails.json");
    }

    public static Path defaultsWildlifeJson() {
        return ROOT.resolve("defaults/wildlife.json");
    }

    /** User-uploaded trail photos (untracked). */
    public static Path userTrailImagesDir() {
        return ROOT.resolve("images/trails");
    }

    /** User-uploaded wildlife photos (untracked). */
    public static Path userWildlifeImagesDir() {
        return ROOT.resolve("images/wildlife");
    }

    /**
     * If {@code trails.json} / {@code wildlife.json} are missing, copy sample data from
     * {@code defaults/} at the project root when present, otherwise from classpath (
     * {@code wildlifeexplorer/bundled/*.json}) so the app works regardless of current working directory.
     */
    public static void bootstrapUserDataFromDefaultsIfMissing() throws IOException {
        DefaultBundle.copyBundledIntoUserDataIfMissing();
    }

    /** Resolve a path stored in JSON to an absolute filesystem path for I/O. */
    public static Path resolveDataPath(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        Path p = Path.of(stored.trim());
        if (p.isAbsolute()) {
            return p.normalize();
        }
        return ROOT.resolve(p).normalize();
    }

    /** Turn an absolute path into a project-relative string for persistence. */
    public static String toStoredPath(Path absoluteFile) {
        Path abs = absoluteFile.toAbsolutePath().normalize();
        Path rel = ROOT.relativize(abs);
        if (rel.startsWith("..")) {
            return abs.toString();
        }
        return rel.toString().replace('\\', '/');
    }

    /** Safe filename segment for copied uploads. */
    public static String safeFileName(String original) {
        if (original == null || original.isBlank()) {
            return "image.png";
        }
        String base = Path.of(original).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
