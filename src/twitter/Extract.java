
package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extract {

    /**
     * Returns the timespan covering the earliest and latest
     * timestamps in the given nonempty list of tweets.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        Instant earliest = tweets.get(0).getTimestamp();
        Instant latest = tweets.get(0).getTimestamp();

        for (Tweet tweet : tweets) {
            Instant timestamp = tweet.getTimestamp();

            if (timestamp.isBefore(earliest)) {
                earliest = timestamp;
            }

            if (timestamp.isAfter(latest)) {
                latest = timestamp;
            }
        }

        return new Timespan(earliest, latest);
    }

    /**
     * Returns the unique usernames mentioned in the tweet texts.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> users = new HashSet<>();

        Pattern pattern = Pattern.compile(
                "(?<![A-Za-z0-9_])@([A-Za-z0-9_]+)");

        for (Tweet tweet : tweets) {
            Matcher matcher = pattern.matcher(tweet.getText());

            while (matcher.find()) {
                users.add(matcher.group(1).toLowerCase());
            }
        }

        return users;
    }
}
