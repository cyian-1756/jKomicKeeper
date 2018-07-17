package komickeeper;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class searchHelper {

//    public static Map<String, String> parseSearch(String search) {
//        Multimap<String, String> multimap = ArrayListMultimap.create();
//        Map<String, String> toReturn = new HashMap<>();
//        for (String line : search.split(" ")) {
//            if (line.contains(":")) {
//                String[] keyVal = line.split(":");
//                toReturn.put(keyVal[0], keyVal[1]);
//            }
//        }
//        return toReturn;
//    }

    public static Multimap<String, String> parseSearch(String search) {
        Multimap<String, String> toReturn = ArrayListMultimap.create();
        for (String line : search.split(" ")) {
            if (line.contains(":")) {
                String[] keyVal = line.split(":");
                toReturn.put(keyVal[0], keyVal[1]);
            }
        }
        return toReturn;
    }

    public static String getSearchString(String searchTerm) {
        return searchTerm.replaceAll("\\S*:\\S*", "");
    }
}
