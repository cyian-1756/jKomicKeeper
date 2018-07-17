import com.google.common.collect.Multimap;
import org.junit.Test;
import komickeeper.searchHelper;

import java.util.Map;

public class searchHelperTests {
    void print(Object i) {
        System.out.println(i);
    }

    @Test
    public void testNormalSearch() {
        assert(searchHelper.parseSearch("test").isEmpty());
    }

    @Test
    public void testTagSearch() {
        Multimap<String, String> search = searchHelper.parseSearch("tag:test tag:2 writer:thedude");
        print(search);
        assert(search.get("tag").equals("test"));
    }

    @Test
    public void testGetSearchString() {
        assert(searchHelper.getSearchString("test tag:this").equals("test "));
        assert(searchHelper.getSearchString("tag:this").equals("test "));
    }
}
