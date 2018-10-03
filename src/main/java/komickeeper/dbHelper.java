package komickeeper;

import com.google.common.collect.Multimap;
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

    public static ObservableList<Comic> getAllComicNames() {
        makeDBIfNotExist(Main.databaseName);
        ObservableList<Comic> usernames = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery("SELECT name,path FROM comics ORDER BY name");
            while ( res.next() ) {
                usernames.add(new Comic(res.getString(1), res.getString(2)));
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

    public static void setTag(String comicPath, String tag) {
        try {
            String tags = null;
            Controller.log("Setting tags for " + comicPath);
            tags = getComicDetails(comicPath).get("tags");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("UPDATE comics SET tags = ? WHERE path = ?");
            if (tags == null) {
                prep.setString(1, tag + ",");
            } else {
                for (String t : tags.split(",")) {
                    if (t.equals(tag)) {
                        // Make sure the tag being added isn't a dupe
                        Controller.log(String.format("%s already has tag \"%s\"", comicPath, tag));
                        return;
                    }
                }
                prep.setString(1, tags + tag + ",");
            }
            prep.setString(2, comicPath);
            prep.executeUpdate();

        } catch(SQLException e) {
            System.err.println("Unable to set tag");
            System.err.println(e.getMessage());
        }
    }

    public static void setRating(String comicPath, String rating) {
        try {
            Controller.log("Setting rating to \"" + rating + "\"");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("UPDATE comics SET rating = ? WHERE path = ?");
            prep.setString(1, rating);
            prep.setString(2, comicPath);
            prep.executeUpdate();
        } catch(SQLException e) {
            System.err.println("Unable to set rating");
            System.err.println(e.getMessage());
        }
    }

    public static void setWriter(String comicPath, String writer) {
        try {
            Controller.log("Setting writer to \"" + writer + "\"");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("UPDATE comics SET writer = ? WHERE path = ?");
            prep.setString(1, writer);
            prep.setString(2, comicPath);
            prep.executeUpdate();
        } catch(SQLException e) {
            System.err.println("Unable to set writer");
            System.err.println(e.getMessage());
        }
    }

    public static String buildTagSearch(String col, List<String> tags) {
        StringBuilder toReturn = new StringBuilder();
        for (int i = 0; i != tags.size(); i++) {
            String tag = tags.get(i);
            toReturn.append(String.format("instr(UPPER(%s), UPPER(\"%s,\"))>0 ", col, tag.toUpperCase()));
            if (i != tags.size() -1) {
                toReturn.append("AND ");
            }
        }
        return toReturn.toString();
    }
    private static List<String> searchToArrayList(String key, Multimap<String, String> searchMap) {
        return new ArrayList<String>(searchMap.get(key));
    }

    public static ObservableList<Comic> searchComics(String rawSearch) {
        ObservableList<Comic> comics = FXCollections.observableArrayList();
        // Get any search tags from the rawSearch
        Multimap<String, String> parsedSearch = searchHelper.parseSearch(rawSearch);
        // Get the search term from the raw search
        String searchTerm = searchHelper.getSearchString(rawSearch);
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep;
            if (!parsedSearch.isEmpty()) {
                String sqlQ = "SELECT * FROM comics WHERE ";
                if (!searchTerm.isEmpty()) {
                    sqlQ+= "instr(UPPER(name), UPPER(?))>0 AND ";
                }
                Controller.log(sqlQ + buildTagSearch("tags", searchToArrayList("tag", parsedSearch)));
                prep = connection.prepareStatement(sqlQ + buildTagSearch("tags", searchToArrayList("tag", parsedSearch)));
            } else if (parsedSearch.isEmpty() && !searchTerm.isEmpty()) {
                Controller.log("Tagless search");
                prep = connection.prepareStatement("SELECT * FROM comics WHERE instr(UPPER(name), UPPER(?))>0 ORDER BY name");
            } else {
                prep = connection.prepareStatement("SELECT * FROM comics ORDER BY name");
            }
            // Only set the searchTerm if the user isn't searching solely tags
            if (!searchTerm.isEmpty()) {
                Controller.log("Binding search term");
                prep.setString(1, searchTerm);
            }
            ResultSet res = prep.executeQuery();
            while ( res.next() ) {
                comics.add(new Comic(res.getString(sqliteName), res.getString(sqlitePath)));
            }
            return comics;
        } catch(SQLException e) {
            System.err.println("Unable to search database");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static Map<String, String> getComicDetails(String comicPath) {
        Map<String, String> comic = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("SELECT * FROM comics WHERE path = ?");
            prep.setString(1, comicPath);
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
            System.err.println("Unable to comic details for " + comicPath);
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void removeComicFromDB(String comicPath) {
         try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + configHelper.getDatabasePath());
            PreparedStatement prep = connection.prepareStatement("DELETE FROM comics WHERE path = ?");
            prep.setString(1, comicPath);
            prep.executeUpdate();

        } catch(SQLException e) {
            System.err.println("Unable to delete comic " + comicPath);
            System.err.println(e.getMessage());
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
