import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Single-file implementation examples for Programming in Java Assignment 5 (CO5).
 * Name: Jothika.S.U. | Register No: 192524122
 * Compile: javac Assignment5_Jothika.java
 * Run: java Assignment5_Jothika
 * JDBC methods require a configured MySQL database and Connector/J driver.
 */
public class Assignment5_Jothika {
    // -------------------- B1: Buffered text-file pass store --------------------
    static class PassStore {
        private final Path file;
        PassStore(Path file) { this.file = file; }

        void add(String passId, String regNo, String name, int route, double amount) throws IOException {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(String.join(",", passId, regNo, name, String.valueOf(route), String.valueOf(amount)));
                w.newLine();
            }
        }

        String[] find(String passId) throws IOException {
            if (!Files.exists(file)) return null;
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    if (fields.length != 5) continue;
                    try {
                        Integer.parseInt(fields[3]);
                        Double.parseDouble(fields[4]);
                    } catch (NumberFormatException badRecord) { continue; }
                    if (fields[0].equals(passId)) return fields;
                }
            }
            return null;
        }

        List<String[]> listByRoute(int route) throws IOException {
            List<String[]> result = new ArrayList<>();
            if (!Files.exists(file)) return result;
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] f = line.split(",", -1);
                    if (f.length != 5) continue;
                    try {
                        int rNo = Integer.parseInt(f[3]);
                        Double.parseDouble(f[4]);
                        if (rNo == route) result.add(f);
                    } catch (NumberFormatException ignored) { /* skip malformed row */ }
                }
            }
            return result;
        }
    }

    // Buffered line counter (counts LF bytes; suitable for standard text files).
    static long countLinesBuffered(Path file) throws IOException {
        long lines = 0;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            int b;
            while ((b = in.read()) != -1) if (b == '\n') lines++;
        }
        return lines;
    }

    // Unbuffered comparison version.
    static long countLinesUnbuffered(Path file) throws IOException {
        long lines = 0;
        try (FileInputStream in = new FileInputStream(file.toFile())) {
            int b;
            while ((b = in.read()) != -1) if (b == '\n') lines++;
        }
        return lines;
    }

    // -------------------- B2: SQL login illustration --------------------
    // Demonstration only; never use the unsafe method in a real application.
    static boolean unsafeLogin(Connection con, String id, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + password + "'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    static boolean safeLogin(Connection con, String id, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=? AND pwd=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // -------------------- B3: Transactional batch insert --------------------
    static int loadPasses(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        int count = 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : passes) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.addBatch();
                count++;
                if (count % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch(); // flush final partial batch
            con.commit();
            return count;
        } catch (SQLException | RuntimeException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    static void createBusPassTable(Connection con) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS bus_pass (" +
                "pass_id VARCHAR(20) PRIMARY KEY, reg_no VARCHAR(20), name VARCHAR(100), " +
                "route_no INT, amount DOUBLE)";
        try (Statement st = con.createStatement()) { st.executeUpdate(sql); }
    }

    static void inspectMetadata(Connection con) throws SQLException {
        DatabaseMetaData md = con.getMetaData();
        System.out.println("Database: " + md.getDatabaseProductName());
        System.out.println("Database version: " + md.getDatabaseProductVersion());
        System.out.println("Driver: " + md.getDriverName());
        System.out.println("Driver version: " + md.getDriverVersion());
        try (ResultSet tables = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
            System.out.println("Tables:");
            while (tables.next()) System.out.println(" - " + tables.getString("TABLE_NAME"));
        }
    }

    // Reads the simple five-column CSV-like file and reports invalid records.
    static List<String[]> readValidPasses(Path file) throws IOException {
        List<String[]> valid = new ArrayList<>();
        int lineNo = 0;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                lineNo++;
                String[] f = line.split(",", -1);
                try {
                    if (f.length != 5 || f[0].isBlank() || f[1].isBlank() || f[2].isBlank())
                        throw new IllegalArgumentException("wrong field count/empty field");
                    Integer.parseInt(f[3]);
                    Double.parseDouble(f[4]);
                    valid.add(f);
                } catch (RuntimeException ex) {
                    System.out.println("Rejected line " + lineNo + ": " + ex.getMessage() + " | " + line);
                }
            }
        }
        return valid;
    }

    // -------------------- Main: runnable file-store demonstration --------------------
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("assignment5_passes", ".csv");
        PassStore store = new PassStore(file);
        store.add("P1001", "192524122", "Jothika", 12, 4500.0);
        Files.writeString(file, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192524123", "Divya", 7, 3800.0);

        System.out.println("Find P1003: " + Arrays.toString(store.find("P1003")));
        System.out.println("Find P1002: " + Arrays.toString(store.find("P1002")));
        System.out.println("Route 12 passes:");
        for (String[] p : store.listByRoute(12)) System.out.println(Arrays.toString(p));
        System.out.println("Buffered line count: " + countLinesBuffered(file));
        System.out.println("Unbuffered line count: " + countLinesUnbuffered(file));
        System.out.println("Sample file: " + file);
        System.out.println("\nJDBC methods are available in this source file, but need your MySQL connection details and Connector/J driver to run.");
    }
}
