package komickeeper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dbHelper {



    public static String getComicType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith("cbz")) {
            return "cbz";
        } else if (fileName.endsWith("cbr")) {
            return "cbr";
        } else if (fileName.endsWith("cbt")) {
            return "cbt";
        } else {
            return "unknown";
        }
    }

    public static ObservableList<String> getAllComicNames() {
        makeDBIfNotExist(Main.databaseName);
        ObservableList<String> usernames = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery("SELECT name FROM comics ORDER BY name");
            while ( res.next() ) {
                usernames.add(res.getString(1));
            }
            return usernames;
        } catch(SQLException e) {
            System.err.println("Unable to get all comics from database");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static List<Map<String, String>> getAllComicsDetails() {
        List<Map<String, String>> val = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM comics");
            while ( res.next() ) {
                Map<String, String> comic = new HashMap<>();
                comic.put("name", res.getString("name"));
                comic.put("path", res.getString("path"));
                comic.put("folder_path", res.getString("folder_path"));
                comic.put("type", res.getString("type"));
                comic.put("size", res.getString("size"));
                comic.put("tags", res.getString("tags"));
                comic.put("rating", res.getString("rating"));
                comic.put("writer", res.getString("writer"));
                val.add(comic);
            }
            return val;
        } catch(SQLException e) {
            System.err.println("Unable to get all comics from database");
            System.err.println(e.getMessage());
            return null;
        }

    }

    private static final int sqliteName = 2;
    private static final int sqlitePath = 3;
    private static final int sqliteFolderPath = 4;
    private static final int sqliteType = 5;
    private static final int sqliteSize = 6;
    private static final int sqliteTags = 7;
    private static final int sqlitePublisher = 8;
    private static final int sqliteRating = 9;
    private static final int sqliteWriter = 10;
    private static final int sqliteRelaseDate = 11;
    private static final int sqliteSeriesName = 12;
    private static final int sqliteTotalPages = 13;
    private static final int sqliteHash = 14;
    private static final int sqliteDateIndexed = 15;

    static String youtubeVideosSQL = "CREATE TABLE videos (\n" +
            "  id   INTEGER PRIMARY KEY,\n" +
            "  name TEXT NOT NULL,\n" +
            "  path TEXT NOT NULL UNIQUE,\n" +
            "  folder_path TEXT NOT NULL,\n" +
            "  type TEXT NOT NULL,\n" +
            "  size INT NOT NULL,\n" +
            "  tags TEXT,\n" +
            "  channel TEXT,\n" +
            "  rating INT,\n" +
            "  writer TEXT,\n" +
            "  release_date TEXT,\n" +
            "  series_name TEXT\n" +
            ");";

    public static boolean dbExists(String dbName) {
        return new File(configHelper.getDatabaseDir() + File.separator + dbName).isFile();
    }

    public static void createNewDatabase(String fileName, String sql) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }

        String url = "jdbc:sqlite:" + configHelper.getDatabaseDir() + File.separator + fileName;

        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            connection.close();
            Controller.log("Created database: " + fileName);
            Controller.log("Sql used: " + sql);

        } catch (SQLException e) {
            System.err.println("Unable to create comic DB: " + configHelper.getDatabaseDir() + File.separator + fileName);
            System.err.println(e.getMessage());
        }
    }

    public static void setTag(String comicName, String tag) {
        try {
            String tags = null;
            Controller.log("Setting tags");
            tags = getComicDetails(comicName).get("tags");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("UPDATE comics SET tags = ? WHERE name = ?");
            if (tags == null) {
                Controller.log("tags is null");
                prep.setString(1, tag);
            } else {
                prep.setString(1, tags + "," + tag);
            }
            prep.setString(2, comicName);
            prep.executeUpdate();

        } catch(SQLException e) {
            System.err.println("Unable to set tag");
            System.err.println(e.getMessage());
        }
    }

    public static void setRating(String comicName, String rating) {
        try {
            Controller.log("Setting rating to \"" + rating + "\"");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("UPDATE comics SET rating = ? WHERE name = ?");
            prep.setString(1, rating);
            prep.setString(2, comicName);
            prep.executeUpdate();
        } catch(SQLException e) {
            System.err.println("Unable to set rating");
            System.err.println(e.getMessage());
        }
    }

    public static void setWriter(String comicName, String writer) {
        try {
            Controller.log("Setting writer to \"" + writer + "\"");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("UPDATE comics SET writer = ? WHERE name = ?");
            prep.setString(1, writer);
            prep.setString(2, comicName);
            prep.executeUpdate();
        } catch(SQLException e) {
            System.err.println("Unable to set writer");
            System.err.println(e.getMessage());
        }
    }

    public static ObservableList<String> searchComics(String searchTerm, Boolean searchTags) {
        ObservableList<String> comics = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep;
            if (searchTags) {
                prep = connection.prepareStatement("SELECT * FROM comics WHERE instr(UPPER(tags), UPPER(?))>0 ORDER BY name");
            } else {
                prep = connection.prepareStatement("SELECT * FROM comics WHERE instr(UPPER(name), UPPER(?))>0 ORDER BY name");
            }
            prep.setString(1, searchTerm);
            ResultSet res = prep.executeQuery();
            while ( res.next() ) {
                comics.add(res.getString(2));
            }
            return comics;
        } catch(SQLException e) {
            System.err.println("Unable to search database");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static Map<String, String> getComicDetails(String comicName) {
        Map<String, String> comic = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("SELECT * FROM comics WHERE name = ?");
            prep.setString(1, comicName);
            ResultSet res = prep.executeQuery();
            while ( res.next() ) {
                comic.put("name", res.getString("name"));
                comic.put("path", res.getString("path"));
                comic.put("folder_path", res.getString("folder_path"));
                comic.put("type", res.getString("type"));
                comic.put("size", res.getString("size"));
                comic.put("tags", res.getString("tags"));
                comic.put("rating", res.getString("rating"));
                comic.put("writer", res.getString("writer"));
                comic.put("date_indexed", getComicIndexDate(res.getLong("date_indexed")));
            }
            return comic;
        } catch(SQLException e) {
            System.err.println("Unable to comic details for " + comicName);
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void makeDBIfNotExist(String dbName) {
        if (!dbExists(dbName)) {
            String comicsSQL = "CREATE TABLE comics (\n" +
                    "  id  INTEGER PRIMARY KEY,\n" +
                    "  name TEXT NOT NULL,\n" +
                    "  path TEXT NOT NULL,\n" +
                    "  folder_path TEXT NOT NULL,\n" +
                    "  type TEXT NOT NULL,\n" +
                    "  size INT NOT NULL,\n" +
                    "  tags TEXT,\n" +
                    "  publisher TEXT,\n" +
                    "  rating INT,\n" +
                    "  writer TEXT,\n" +
                    "  release_date TEXT,\n" +
                    "  series_name TEXT,\n" +
                    "  total_pages INT,\n" +
                    "  hash TEXT,\n" +
                    "  date_indexed LONG,\n" +
                    "  unique (path, name)\n" +
                    ");";
            createNewDatabase(dbName, comicsSQL);
        }
    }

    private static String getComicIndexDate(Long d) {
        java.util.Date time=new java.util.Date((long)d*1000);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    private static String sanitizeComicName(String comicName) {
        comicName = comicName.replaceAll("\\([a-zA-Z\\- ]+\\)", "");
        comicName = comicName.replaceAll("  .cb", ".cb");
        comicName = comicName.replaceAll("_", " ");
        return comicName;
    }

    private static String calcSHA1(File file) throws IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }
    public static void writeComicsToDB(List<File> files) {
        makeDBIfNotExist(Main.databaseName);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            connection.setAutoCommit(false);
            PreparedStatement prep = connection.prepareStatement("INSERT OR IGNORE INTO comics VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            for (File comic : files) {
                Controller.log("Writing comic with name " + comic.getName() + " to DB");
                // create a database connection
                Statement statement = connection.createStatement();
                statement.setQueryTimeout(30);  // set timeout to 30 sec.
                prep.setString(sqliteName, sanitizeComicName(comic.getName()));
                prep.setString(sqlitePath, comic.getAbsolutePath());
                prep.setString(sqliteFolderPath, comic.getParent());
                prep.setString(sqliteType, getComicType(comic));
                prep.setLong(sqliteSize, comic.length());
                prep.setLong(sqliteDateIndexed, Instant.now().getEpochSecond());
                prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            connection.close();
            Controller.log("Done writing comics");
        }
        catch(SQLException e) {
            System.err.println("Unable to write comics to database " + configHelper.getDatabasePath());
            System.err.println(e.getMessage());
        }
    }
}
