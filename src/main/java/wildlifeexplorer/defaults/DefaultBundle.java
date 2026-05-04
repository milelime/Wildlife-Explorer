package wildlifeexplorer.defaults;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import wildlifeexplorer.AppPaths;

/**
 * Sample data shipped inside the JAR / classpath ({@code wildlifeexplorer/bundled/*.json}) with a
 * filesystem fallback to {@code defaults/} at the project root for development.
 */
public final class DefaultBundle {

    private static final String CP_TRAILS = "wildlifeexplorer/bundled/trails.json";
    private static final String CP_WILDLIFE = "wildlifeexplorer/bundled/wildlife.json";

    private DefaultBundle() {}

    /** Copy bundled trails/wildlife JSON into the working-copy paths when those files are missing. */
    public static void copyBundledIntoUserDataIfMissing() throws IOException {
        copyOne(AppPaths.userTrailsJson(), AppPaths.defaultsTrailsJson(), CP_TRAILS);
        copyOne(AppPaths.userWildlifeJson(), AppPaths.defaultsWildlifeJson(), CP_WILDLIFE);
    }

    private static void copyOne(Path userPath, Path filesystemDefault, String classpathResource)
        throws IOException {
        if (Files.exists(userPath)) {
            return;
        }
        byte[] bytes;
        if (Files.exists(filesystemDefault)) {
            bytes = Files.readAllBytes(filesystemDefault);
        } else {
            bytes = readClasspath(classpathResource);
            if (bytes == null) {
                throw new IOException(
                    "Missing sample data: neither defaults/ at project root nor classpath resource "
                        + classpathResource
                        + " is available.");
            }
        }
        Path parent = userPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(userPath, bytes);
    }

    /** Text used to compare working copies against the canonical bundle (filesystem or classpath). */
    public static String bundledTrailsJsonForCompare() throws IOException {
        return readBundledUtf8(AppPaths.defaultsTrailsJson(), CP_TRAILS);
    }

    /** @see #bundledTrailsJsonForCompare() */
    public static String bundledWildlifeJsonForCompare() throws IOException {
        return readBundledUtf8(AppPaths.defaultsWildlifeJson(), CP_WILDLIFE);
    }

    private static String readBundledUtf8(Path filesystemDefault, String classpathResource)
        throws IOException {
        if (Files.exists(filesystemDefault)) {
            return Files.readString(filesystemDefault, StandardCharsets.UTF_8);
        }
        byte[] bytes = readClasspath(classpathResource);
        if (bytes == null) {
            throw new IOException("Missing bundled reference: " + classpathResource);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] readClasspath(String classpathResource) throws IOException {
        ClassLoader cl = DefaultBundle.class.getClassLoader();
        try (InputStream in = cl == null ? null : cl.getResourceAsStream(classpathResource)) {
            if (in == null) {
                return null;
            }
            return in.readAllBytes();
        }
    }
}
