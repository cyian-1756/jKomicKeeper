package komickeeper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class searchHelper {

    /**
     * Takes a string and parses it into a multuimap. Used for tag searches
     *
     * @param search The string to parse into a multimap
     * @return a multimap of tags
     */
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

    /**
    * Takes a string removes all tags in the format of TAG:VALUE
    *
    * @param searchTerm The string that will have tags removed
    */
    public static String getSearchString(String searchTerm) {
        return searchTerm.replaceAll("\\S*:\\S*", "");
    }
}
