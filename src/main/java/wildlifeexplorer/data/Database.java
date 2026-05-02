package wildlifeexplorer.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import wildlifeexplorer.model.Trail;
import wildlifeexplorer.model.Wildlife;

public class Database {
    private static final int SCHEMA_VERSION = 1;

    private final List<Trail> trails;
    private final Map<Integer, Wildlife> wildlifeById;

    private final Path trailsPath;
    private final Path wildlifePath;

    public Database(String trailsFileName, String wildlifeFileName) {
        this.trailsPath = Path.of(trailsFileName);
        this.wildlifePath = Path.of(wildlifeFileName);
        this.trails = new ArrayList<>();
        this.wildlifeById = new LinkedHashMap<>();
        load();

        // Ensure the database files exist even before any mutations.
        if (!Files.exists(trailsPath) || !Files.exists(wildlifePath)) {
            save();
        }
    }

    public List<Trail> getAllTrails() {
        return trails;
    }

    public List<Wildlife> getAllWildlife() {
        return new ArrayList<>(wildlifeById.values());
    }

    public void insertTrail(Trail trail) {
        trails.add(trail);
        save();
    }

    public void upsertTrail(Trail trail) {
        Trail existing = fetchTrailById(trail.getId());
        if (existing == null) {
            trails.add(trail);
        } else {
            trails.remove(existing);
            trails.add(trail);
        }
        save();
    }

    public Trail fetchTrailById(int id) {
        for (Trail t : trails) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Trail> findTrails(String criteria) {
        String q = criteria == null ? "" : criteria.toLowerCase();
        List<Trail> result = new ArrayList<>();
        for (Trail t : trails) {
            if (t.getName().toLowerCase().contains(q)) {
                result.add(t);
            }
        }
        return result;
    }

    public void insertRating(int trailId, double score) {
        Trail t = fetchTrailById(trailId);
        if (t == null) {
            throw new IllegalArgumentException("Trail not found: " + trailId);
        }
        t.updateRating(score);
        save();
    }

    public void insertWildlife(int trailId, Wildlife wildlife) {
        Trail t = fetchTrailById(trailId);
        if (t == null) {
            throw new IllegalArgumentException("Trail not found: " + trailId);
        }
        Wildlife canonical = upsertWildlife(wildlife);
        t.addWildlife(canonical);
        save();
    }

    /** Links an existing catalog wildlife entry to this trail (no duplicate IDs on the same trail). */
    public void attachExistingWildlifeToTrail(int trailId, int wildlifeId) {
        Trail t = fetchTrailById(trailId);
        if (t == null) {
            throw new IllegalArgumentException("Trail not found: " + trailId);
        }
        Wildlife w = wildlifeById.get(wildlifeId);
        if (w == null) {
            throw new IllegalArgumentException("Wildlife not found: " + wildlifeId);
        }
        for (Wildlife existing : t.getWildlifeList()) {
            if (existing.getId() == wildlifeId) {
                return;
            }
        }
        t.addWildlife(w);
        save();
    }

    /** Removes a wildlife link from this trail only; the global catalog entry is unchanged. */
    public void removeWildlifeFromTrail(int trailId, int wildlifeId) {
        Trail t = fetchTrailById(trailId);
        if (t == null) {
            throw new IllegalArgumentException("Trail not found: " + trailId);
        }
        t.getWildlifeList().removeIf(w -> w.getId() == wildlifeId);
        save();
    }

    public void load() {
        trails.clear();
        wildlifeById.clear();

        try {
            loadWildlifeFile();
            loadTrailsFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read database files", e);
        }
    }

    public void save() {
        saveWildlifeFile();
        saveTrailsFile();
    }

    /** Removes every trail and wildlife record and persists empty JSON files. */
    public void clearAllUserData() {
        trails.clear();
        wildlifeById.clear();
        save();
    }

    //remove an existing trail entirely 
    public void deleteTrail(int id) {
        Trail trail = fetchTrailById(id);
        if (trail == null){
            throw new IllegalArgumentException("Trail not found: " + id);
        }
        trails.remove(trail);
        save();
    }

    /** Parses JSON text using the same parser as persisted database files (for tooling / comparisons). */
    public static Object parseJsonDocument(String json) {
        return Json.parse(json);
    }

    private void loadWildlifeFile() throws IOException {
        if (!Files.exists(wildlifePath)) {
            return;
        }
        String json = Files.readString(wildlifePath, StandardCharsets.UTF_8);
        if (json.isBlank()) {
            return;
        }
        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map)) {
            return;
        }
        Map<?, ?> root = (Map<?, ?>) parsed;
        Object wildlifeObj = root.get("wildlife");
        if (!(wildlifeObj instanceof List)) {
            return;
        }
        for (Object wObj : (List<?>) wildlifeObj) {
            Wildlife w = fromJsonWildlife(wObj);
            if (w != null) {
                wildlifeById.put(w.getId(), w);
            }
        }
    }

    private void loadTrailsFile() throws IOException {
        if (!Files.exists(trailsPath)) {
            return;
        }
        String json = Files.readString(trailsPath, StandardCharsets.UTF_8);
        if (json.isBlank()) {
            return;
        }
        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map)) {
            return;
        }
        Map<?, ?> root = (Map<?, ?>) parsed;
        Object trailsObj = root.get("trails");
        if (!(trailsObj instanceof List)) {
            return;
        }

        for (Object trailObj : (List<?>) trailsObj) {
            Trail t = fromJsonTrailWithRefs(trailObj);
            if (t != null) {
                trails.add(t);
            }
        }
    }

    private void saveTrailsFile() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", SCHEMA_VERSION);
        root.put("updatedAt", Instant.now().toString());

        List<Object> trailJson = new ArrayList<>();
        for (Trail t : trails) {
            trailJson.add(toJsonTrail(t));
        }
        root.put("trails", trailJson);

        String json = Json.stringify(root) + "\n";
        try {
            Files.writeString(trailsPath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write trails file: " + trailsPath, e);
        }
    }

    private void saveWildlifeFile() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", SCHEMA_VERSION);
        root.put("updatedAt", Instant.now().toString());

        List<Object> wildlifeJson = new ArrayList<>();
        for (Wildlife w : wildlifeById.values()) {
            wildlifeJson.add(toJsonWildlife(w));
        }
        root.put("wildlife", wildlifeJson);

        String json = Json.stringify(root) + "\n";
        try {
            Files.writeString(wildlifePath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write wildlife file: " + wildlifePath, e);
        }
    }

    private static Map<String, Object> toJsonTrail(Trail t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", t.getName());
        m.put("id", t.getId());
        m.put("length", t.getLength());
        m.put("elevation", t.getElevation());
        m.put("rating", t.getRating());
        m.put("totalRating", t.getTotalRating());
        m.put("numRating", t.getNumRating());
        m.put("pictures", new ArrayList<>(t.getPictures()));

        List<Object> wildlifeIds = new ArrayList<>();
        for (Wildlife w : t.getWildlifeList()) {
            wildlifeIds.add(w.getId());
        }
        m.put("wildlifeIds", wildlifeIds);
        return m;
    }

    private static Map<String, Object> toJsonWildlife(Wildlife w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", w.getName());
        m.put("id", w.getId());
        m.put("description", w.getDescription());
        m.put("notes", w.getNotes());
        m.put("pictures", new ArrayList<>(w.getPictures()));
        return m;
    }

    private static Trail fromJsonTrail(Object obj) {
        // (legacy) old trail file format embedded wildlifeList
        if (!(obj instanceof Map)) {
            return null;
        }
        Map<?, ?> m = (Map<?, ?>) obj;

        String name = asString(m.get("name"), "");
        int id = asInt(m.get("id"), 0);
        double length = asDouble(m.get("length"), 0.0);
        double elevation = asDouble(m.get("elevation"), 0.0);

        Trail t = new Trail(name, id, length, elevation);

        double totalRating = asDouble(m.get("totalRating"), 0.0);
        int numRating = asInt(m.get("numRating"), 0);
        t.setRatingState(totalRating, numRating);

        Object picturesObj = m.get("pictures");
        if (picturesObj instanceof List) {
            for (Object p : (List<?>) picturesObj) {
                t.addPicture(asString(p, ""));
            }
        }

        Object wildlifeObj = m.get("wildlifeList");
        if (wildlifeObj instanceof List) {
            for (Object wObj : (List<?>) wildlifeObj) {
                Wildlife w = fromJsonWildlife(wObj);
                if (w != null) {
                    t.addWildlife(w);
                }
            }
        }

        return t;
    }

    private Trail fromJsonTrailWithRefs(Object obj) {
        if (!(obj instanceof Map)) {
            return null;
        }
        Map<?, ?> m = (Map<?, ?>) obj;

        String name = asString(m.get("name"), "");
        int id = asInt(m.get("id"), 0);
        double length = asDouble(m.get("length"), 0.0);
        double elevation = asDouble(m.get("elevation"), 0.0);

        Trail t = new Trail(name, id, length, elevation);

        double totalRating = asDouble(m.get("totalRating"), 0.0);
        int numRating = asInt(m.get("numRating"), 0);
        t.setRatingState(totalRating, numRating);

        Object picturesObj = m.get("pictures");
        if (picturesObj instanceof List) {
            for (Object p : (List<?>) picturesObj) {
                String pic = asString(p, "");
                if (!pic.isEmpty()) {
                    t.addPicture(pic);
                }
            }
        }

        Object wildlifeIdsObj = m.get("wildlifeIds");
        if (wildlifeIdsObj instanceof List) {
            for (Object wid : (List<?>) wildlifeIdsObj) {
                int wildlifeId = asInt(wid, 0);
                Wildlife w = wildlifeById.get(wildlifeId);
                if (w != null) {
                    t.addWildlife(w);
                }
            }
        }

        return t;
    }

    private Wildlife upsertWildlife(Wildlife wildlife) {
        int id = wildlife.getId();
        if (id <= 0) {
            id = nextWildlifeId();
        }

        Wildlife canonical = wildlifeById.get(id);
        if (canonical == null) {
            canonical = new Wildlife(wildlife.getName(), id, wildlife.getDescription(), wildlife.getNotes());
            for (String pic : wildlife.getPictures()) {
                canonical.addPictures(pic);
            }
            wildlifeById.put(id, canonical);
            return canonical;
        }

        // Update canonical record (simple overwrite strategy)
        canonical.setName(wildlife.getName());
        canonical.setDescription(wildlife.getDescription());
        canonical.setNotes(wildlife.getNotes());
        for (String pic : wildlife.getPictures()) {
            if (!canonical.getPictures().contains(pic)) {
                canonical.addPictures(pic);
            }
        }
        return canonical;
    }

    private int nextWildlifeId() {
        int max = 0;
        for (int id : wildlifeById.keySet()) {
            if (id > max) max = id;
        }
        return max + 1;
    }

    private static Wildlife fromJsonWildlife(Object obj) {
        if (!(obj instanceof Map)) {
            return null;
        }
        Map<?, ?> m = (Map<?, ?>) obj;
        String name = asString(m.get("name"), "");
        int id = asInt(m.get("id"), 0);
        String description = asString(m.get("description"), "");
        String notes = asString(m.get("notes"), "");

        Wildlife w = new Wildlife(name, id, description, notes);
        Object picturesObj = m.get("pictures");
        if (picturesObj instanceof List) {
            for (Object p : (List<?>) picturesObj) {
                w.addPictures(asString(p, ""));
            }
        }
        return w;
    }

    private static String asString(Object v, String fallback) {
        return v instanceof String ? (String) v : fallback;
    }

    private static int asInt(Object v, int fallback) {
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return fallback;
    }

    private static double asDouble(Object v, double fallback) {
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        return fallback;
    }

    /**
     * Tiny JSON parser/stringifier (objects, arrays, strings, numbers, booleans, null).
     * Kept internal to avoid adding external dependencies.
     */
    static final class Json {
        static Object parse(String s) {
            return new Parser(s).parseValue();
        }

        static String stringify(Object v) {
            StringBuilder sb = new StringBuilder();
            writeJson(sb, v);
            return sb.toString();
        }

        private static void writeJson(StringBuilder sb, Object v) {
            if (v == null) {
                sb.append("null");
            } else if (v instanceof String) {
                sb.append('"').append(escape((String) v)).append('"');
            } else if (v instanceof Boolean) {
                sb.append(((Boolean) v) ? "true" : "false");
            } else if (v instanceof Number) {
                sb.append(v.toString());
            } else if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) v;
                sb.append('{');
                boolean first = true;
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    sb.append('"').append(escape(e.getKey())).append('"').append(':');
                    writeJson(sb, e.getValue());
                }
                sb.append('}');
            } else if (v instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> a = (List<Object>) v;
                sb.append('[');
                for (int i = 0; i < a.size(); i++) {
                    if (i > 0) sb.append(',');
                    writeJson(sb, a.get(i));
                }
                sb.append(']');
            } else {
                sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
        }

        private static String escape(String s) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"':
                        out.append("\\\"");
                        break;
                    case '\\':
                        out.append("\\\\");
                        break;
                    case '\b':
                        out.append("\\b");
                        break;
                    case '\f':
                        out.append("\\f");
                        break;
                    case '\n':
                        out.append("\\n");
                        break;
                    case '\r':
                        out.append("\\r");
                        break;
                    case '\t':
                        out.append("\\t");
                        break;
                    default:
                        if (c < 0x20) {
                            out.append(String.format("\\u%04x", (int) c));
                        } else {
                            out.append(c);
                        }
                }
            }
            return out.toString();
        }

        private static final class Parser {
            private final String s;
            private int i = 0;

            Parser(String s) {
                this.s = s;
            }

            Object parseValue() {
                skipWs();
                if (i >= s.length()) {
                    return null;
                }
                char c = s.charAt(i);
                if (c == '{') return parseObject();
                if (c == '[') return parseArray();
                if (c == '"') return parseString();
                if (c == 't' || c == 'f') return parseBoolean();
                if (c == 'n') return parseNull();
                return parseNumber();
            }

            private Map<String, Object> parseObject() {
                expect('{');
                skipWs();
                Map<String, Object> m = new LinkedHashMap<>();
                if (peek('}')) {
                    expect('}');
                    return m;
                }
                while (true) {
                    skipWs();
                    String key = parseString();
                    skipWs();
                    expect(':');
                    Object value = parseValue();
                    m.put(key, value);
                    skipWs();
                    if (peek('}')) {
                        expect('}');
                        return m;
                    }
                    expect(',');
                }
            }

            private List<Object> parseArray() {
                expect('[');
                skipWs();
                List<Object> a = new ArrayList<>();
                if (peek(']')) {
                    expect(']');
                    return a;
                }
                while (true) {
                    Object v = parseValue();
                    a.add(v);
                    skipWs();
                    if (peek(']')) {
                        expect(']');
                        return a;
                    }
                    expect(',');
                }
            }

            private String parseString() {
                expect('"');
                StringBuilder out = new StringBuilder();
                while (i < s.length()) {
                    char c = s.charAt(i++);
                    if (c == '"') {
                        return out.toString();
                    }
                    if (c == '\\') {
                        if (i >= s.length()) break;
                        char e = s.charAt(i++);
                        switch (e) {
                            case '"':
                                out.append('"');
                                break;
                            case '\\':
                                out.append('\\');
                                break;
                            case '/':
                                out.append('/');
                                break;
                            case 'b':
                                out.append('\b');
                                break;
                            case 'f':
                                out.append('\f');
                                break;
                            case 'n':
                                out.append('\n');
                                break;
                            case 'r':
                                out.append('\r');
                                break;
                            case 't':
                                out.append('\t');
                                break;
                            case 'u':
                                out.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                                i += 4;
                                break;
                            default:
                                out.append(e);
                        }
                    } else {
                        out.append(c);
                    }
                }
                throw new IllegalArgumentException("Unterminated string");
            }

            private Boolean parseBoolean() {
                if (s.startsWith("true", i)) {
                    i += 4;
                    return true;
                }
                if (s.startsWith("false", i)) {
                    i += 5;
                    return false;
                }
                throw new IllegalArgumentException("Invalid boolean at " + i);
            }

            private Object parseNull() {
                if (s.startsWith("null", i)) {
                    i += 4;
                    return null;
                }
                throw new IllegalArgumentException("Invalid null at " + i);
            }

            private Number parseNumber() {
                int start = i;
                if (peek('-')) i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                boolean isFloat = false;
                if (peek('.')) {
                    isFloat = true;
                    i++;
                    while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                }
                if (peek('e') || peek('E')) {
                    isFloat = true;
                    i++;
                    if (peek('+') || peek('-')) i++;
                    while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                }
                String num = s.substring(start, i);
                try {
                    if (isFloat) return Double.parseDouble(num);
                    long l = Long.parseLong(num);
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                    return l;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number at " + start + ": " + num);
                }
            }

            private void skipWs() {
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                        i++;
                    } else {
                        return;
                    }
                }
            }

            private boolean peek(char c) {
                return i < s.length() && s.charAt(i) == c;
            }

            private void expect(char c) {
                skipWs();
                if (i >= s.length() || s.charAt(i) != c) {
                    throw new IllegalArgumentException("Expected '" + c + "' at " + i);
                }
                i++;
            }
        }
    }
}
