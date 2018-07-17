import komickeeper.Controller;
import komickeeper.Main;
import komickeeper.dbHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class dbHelperTests {

    @Test
    public void testMakeDB() {
        Main.databaseName = "test";
        dbHelper.makeDBIfNotExist("test");
        assert(new File("./dbs/test").exists());
    }

    @Test
    public void testGetComicType() {
        String cbr = dbHelper.getComicType(new File("./src/test/testFile/test.cbr"));
        String cbz = dbHelper.getComicType(new File("./src/test/testFile/test.cbz"));
        Assert.assertEquals("cbz", cbz);
        Assert.assertEquals("cbr", cbr);
    }

    @Test
    public void testWriteComicsToDB() {
        Main.databaseName = "test";
        dbHelper.makeDBIfNotExist("test");
        Controller.indexComics("./src/test/testFiles");
        Map<String, String> comicInfo = dbHelper.getComicDetails("test.cbr");
        assert comicInfo != null;
        assert(comicInfo.get("name").equals("test.cbr"));
    }

    @Test
    public void testInserts() {
        Main.databaseName = "test";
        dbHelper.setWriter("test.cbr", "writer");
        dbHelper.setRating("test.cbr", "5");
        dbHelper.setTag("test.cbr", "tag");
        Map<String, String> comicInfo = dbHelper.getComicDetails("test.cbr");
        Assert.assertEquals("writer", comicInfo.get("writer"));
        Assert.assertEquals("5", comicInfo.get("rating"));
        Assert.assertEquals("tag", comicInfo.get("tags"));

    }

//    @Test
//    public void testBuildTagSearch() {
//        String[] tags = {"deadpool", "annual"};
//        String f = dbHelper.buildTagSearch("tags", tags);
//        System.out.println(f);
//        assert(f.equals("instr(UPPER(tags), UPPER(\"test\"))>0 AND instr(UPPER(tags), UPPER(\"test2\"))>0 AND instr(UPPER(tags), UPPER(\"test3\"))>0"));
//    }

    @AfterClass
    public static void deleteTestDB() {
        new File("./dbs/test").delete();
    }


}
