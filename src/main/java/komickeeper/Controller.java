package komickeeper;

import com.adarshr.raroscope.RAREntry;
import com.adarshr.raroscope.RARFile;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ini4j.Ini;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.zip.ZipFile;

public class Controller {
    @FXML
    public ListView<String> comicListView;

    @FXML
    private TextArea comicSearchTextField;

    @FXML
    private Label comicNameLabel;

    @FXML
    private Label comicPathLabel;

    @FXML
    private Label comicRatingLabel;

    @FXML
    private Label comicTypeLabel;

    @FXML
    private Label comicSizeLabel;

    @FXML
    private Label comicTagsLabel;

    @FXML
    private TextField comicSetRatingTextField;

    @FXML
    private TextField searchBox;

    @FXML
    private TextField tagsTextField;

    @FXML
    private CheckBox searchTagsCheckBox;

    @FXML
    private TextField comicSetWriterTextField;

    @FXML
    private TextField databaseNameTextfield;

    @FXML
    private Label currentDatabaseLabel;

    @FXML
    private Label comicWriterLabel;

    @FXML
    private Label comicDateIndexedLabel;

    private ObservableList<String> currentlySelectedComics;


    public static void log(String log) {
        if (Main.out != null) {
            Main.out.println(log);
        }
        System.out.println(log);
    }

    private File comicDir;

    @FXML
    private Label currentComicDirLabel;


    private static boolean isComic(String fileName) {
        return fileName.endsWith("cbr") || fileName.endsWith("cbz") || fileName.endsWith("cbt");
    }



    public static List<File> getFilesFromDir(String directoryName) {
        log("Scanning " + directoryName + " for comics");
        File directory = new File(directoryName);
        List<File> files = new ArrayList<>();
        // get all the files from a directory
        File[] fList = directory.listFiles();

        for (File file : fList) {
            if (file.isFile()) {
                if (isComic(file.getName())) {
                    files.add(file);
                }
            } else if (file.isDirectory()) {
                files.addAll(getFilesFromDir(file.getAbsolutePath()));
            }
        }
        log("Found " + files.size() + " comics");
        return files;
    }

    public static void indexComics(String comicPath) {
        List<File> comics = getFilesFromDir(comicPath);
        dbHelper.writeComicsToDB(comics);
    }

    @FXML
    public void updateComicList(ObservableList<String> l) {
        comicListView.setItems(l);
    }

    @FXML
    private void populateComicList() {
        comicListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateComicList(dbHelper.getAllComicNames());
    }

    @FXML
    private void populateSettingLabels() {
        try {
            comicDir = new File(configHelper.getSetting(Main.config, "comicDir", ""));
        } catch (NullPointerException e) {
            log("Not indexing comics as comicDir is null");
        }
        currentComicDirLabel.setText(configHelper.getSetting(Main.config,"comicDir", ""));
        currentDatabaseLabel.setText(configHelper.getSetting(Main.config,"database", "comics"));
    }

    @FXML
    private void indexComicEvent() {
        try {
            indexComics(comicDir.getAbsolutePath());
        } catch (NullPointerException e) {
            log("Not indexing comics as comicDir is null");
        }
    }

    @FXML
    protected void comicSearchEvent(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            updateComicList(dbHelper.searchComics(searchBox.getText()));
        }
    }

    @FXML
    protected void addTags(Event e) {
        for (String comicName : currentlySelectedComics) {
            String tagsToAdd = tagsTextField.getText();
            if (comicName != null && tagsToAdd != null) {
                for (String tag : tagsToAdd.split(",")) {
                    tag = tag.trim();
                    dbHelper.setTag(comicName, tag);
                }
            } else {
                log("Either comicName or tagsToAdd is null");
            }
        }
    }

    @FXML
    public void handleSelectedComicChange(Event arg0) {
        String comicName = comicListView.getSelectionModel().getSelectedItem();
        currentlySelectedComics = comicListView.getSelectionModel().getSelectedItems();
        comicNameLabel.setText(comicName);
        Map<String, String> comicInfo = dbHelper.getComicDetails(comicName);
        int size = Integer.parseInt(comicInfo.get("size")) / 1000 / 1000;
        comicPathLabel.setText(comicInfo.get("path"));
        comicTypeLabel.setText(comicInfo.get("type"));
        comicSizeLabel.setText(Integer.toString(size) + "MB");
        comicRatingLabel.setText(comicInfo.get("rating"));
        comicWriterLabel.setText(comicInfo.get("writer"));
        comicDateIndexedLabel.setText(comicInfo.get("date_indexed"));
        String tags = comicInfo.get("tags");
        if (tags != null) {
            tags = tags.replaceAll(",", ", ");
        }
        comicTagsLabel.setText(tags);
        // Clear our metadata fields
        tagsTextField.clear();
        comicSetRatingTextField.clear();
        comicSetWriterTextField.clear();

    }

    @FXML
    private void getComicDir() {
        final DirectoryChooser fileChooser = new DirectoryChooser();
        File file = fileChooser.showDialog(new Stage());
        if (file != null && file.isDirectory()) {
            comicDir = file;
            configHelper.setSetting("comicDir", file.getAbsolutePath());
            currentComicDirLabel.setText("Comic Dir: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void getDB() {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null && file.isDirectory()) {
            comicDir = file;
            configHelper.setSetting("comicDir", file.getAbsolutePath());
            currentComicDirLabel.setText("Comic Dir: " + file.getAbsolutePath());
        }
    }


    @FXML
    private void openComic(String comicName) {
        new Thread(() -> {
            Map<String, String> comicInfo = dbHelper.getComicDetails(comicName);
            try {
                Desktop.getDesktop().open(new File(comicInfo.get("path")));
            } catch (IOException e) {
                log("Unable to open file: " + comicInfo.get("path") + "\nGot error: " + e.getMessage());
            }
        }).start();

    }

    @FXML
    public void openComicEvent(Event e) {
        openComic(comicListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void openComicFolderEvent(Event e) {
        openComicFolder(comicListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void selectDatabase(Event e) {
        final FileChooser fileChooser = new FileChooser();
        if (new File(configHelper.getDatabaseDir()).canRead()) {
            fileChooser.setInitialDirectory(new File(configHelper.getDatabaseDir()));
        }
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            Main.databaseName = file.getName();
            configHelper.setSetting("database", file.getName());
            currentDatabaseLabel.setText(file.getName());
        }

    }

    @FXML
    public void makeNewDatabase(Event e) {
        String dbName = databaseNameTextfield.getText();
        if (dbName != null && dbName != "") {
            dbHelper.makeDBIfNotExist(dbName + ".sqlite");
        }

    }

    private void openComicFolder(String comicName) {
        new Thread(() -> {
            Map<String, String> comicInfo = dbHelper.getComicDetails(comicName);
            try {
                Desktop.getDesktop().open(new File(comicInfo.get("folder_path")));
            } catch (IOException e) {
                log("Unable to open file: " + comicInfo.get("folder_path") + "\nGot error: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    public void setRatingEvent(Event e) {
        for (String comicName : currentlySelectedComics) {
            dbHelper.setRating(comicName, comicSetRatingTextField.getText());
        }
    }

    @FXML
    public void setWriterEvent(Event e) {
        for (String comicName : currentlySelectedComics) {
            dbHelper.setWriter(comicName, comicSetWriterTextField.getText());
        }
    }

    public List<String> getComicsThatNoLongerExist() {
        List<String> removedFiles = new ArrayList<>();
        List<Map<String, String>> comics = dbHelper.getAllComicsDetails();
        for (Map<String, String> comic : comics) {
            if (!new File(comic.get("path")).exists()) {
                log("There is no comic at: " + comic.get("path"));
                removedFiles.add(comic.get("path"));
            }
        }
        return removedFiles;

    }

    public int numberOfFilesInCBZ(String pathToComic) {
        try {
            ZipFile comic = new ZipFile(pathToComic);
            return comic.size();
        } catch (IOException e) {
            log("Unable to get number of pages in comic " + pathToComic);
            log("Got error: " + e.getMessage());
            return 0;
        }
    }

    public int numberOfFilesInCBR(String pathToComic) {
        log(pathToComic);
        try {
            int x = 0;
            RARFile file = new RARFile(pathToComic);
            Enumeration<RAREntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                try {
                    RAREntry entry = entries.nextElement();
                    x++;
                } catch (NoSuchElementException e) {
                    return x;
                }
            }
            file.close();
            return x;
        } catch (IOException e) {
            log("Unable to get number of pages in comic " + pathToComic);
            log("Got error: " + e.getMessage());
            return 0;
        }
    }

    @FXML
    public void getComicPageNumbers() {
        List<File> comics = getFilesFromDir(comicDir.getAbsolutePath());
        List<Map<String, Integer>> comicPageNumberInfo = new ArrayList<>();
        List<File> cbrComics = new ArrayList<>();
        List<File> cbzComics = new ArrayList<>();
        for (File comic : comics) {
            String comicType = dbHelper.getComicType(comic);
            if (comicType.equals("cbz")) {
                cbzComics.add(comic);
            } else if (comicType.equals("cbr")) {
                cbrComics.add(comic);
            }
        }
        new Thread(() -> {
            for (File comic : cbrComics) {
                Map<String, Integer> cSize = new HashMap<>();
                cSize.put(comic.getAbsolutePath(), numberOfFilesInCBR(comic.getAbsolutePath()));
                System.out.println(comic.getName() + " " + numberOfFilesInCBR(comic.getAbsolutePath()));
                comicPageNumberInfo.add(cSize);
            }
        }).start();
//        new Thread(() -> {
//            for (File comic : cbzComics) {
//                Map<String, Integer> cSize = new HashMap<>();
//                cSize.put(comic.getAbsolutePath(), numberOfFilesInCBZ(comic.getAbsolutePath()));
//                System.out.println(comic.getName() + " " + numberOfFilesInCBZ(comic.getAbsolutePath()));
//                comicPageNumberInfo.add(cSize);
//            }
//        }).start();


    }

}
