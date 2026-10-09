
package twitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Filter {

    /**
     * Returns tweets written by the specified username.
     */
    public static List<Tweet> writtenBy(
            List<Tweet> tweets, String username) {

        List<Tweet> result = new ArrayList<>();

        for (Tweet tweet : tweets) {
            if (tweet.getAuthor().equals(username)) {
                result.add(tweet);
            }
        }

        return result;
    }

    /**
     * Returns tweets whose timestamps fall within the given timespan.
     */
    public static List<Tweet> inTimespan(
            List<Tweet> tweets, Timespan timespan) {

        List<Tweet> result = new ArrayList<>();

        for (Tweet tweet : tweets) {
            Instant timestamp = tweet.getTimestamp();

            boolean afterStart =
                    !timestamp.isBefore(timespan.getStart());

            boolean beforeEnd =
                    !timestamp.isAfter(timespan.getEnd());

            if (afterStart && beforeEnd) {
                result.add(tweet);
            }
        }

        return result;
    }

    /**
     * Returns tweets containing all the specified words.
     */
    public static List<Tweet> containing(
            List<Tweet> tweets, List<String> words) {

        List<Tweet> result = new ArrayList<>();

        for (Tweet tweet : tweets) {
            String text = tweet.getText().toLowerCase();
            boolean containsAll = true;

            for (String word : words) {
                if (!text.contains(word.toLowerCase())) {
                    containsAll = false;
                    break;
                }
            }

            if (containsAll) {
                result.add(tweet);
            }
        }

        return result;
    }
}
