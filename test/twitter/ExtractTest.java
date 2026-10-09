
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy:
     *
     * getTimespan:
     * Partition by list size and timestamp ordering.
     * Test one tweet, chronological order, reverse order,
     * mixed order, and identical timestamps.
     *
     * getMentionedUsers:
     * Partition by zero, one, multiple, and repeated mentions.
     * Also test mentions across different tweets.
     *
     * Use small artificial tweets with known timestamps and text.
     */

	private Tweet makeTweet(String author, String text, String time) {
	    return new Tweet(1L, author, text, Instant.parse(time));
	}
    // ---------------- getTimespan tests ----------------

    @Test
    public void testGetTimespanOneTweet() {
        Tweet t = makeTweet("alice", "Hello",
                "2025-01-01T10:00:00Z");

        Timespan span = Extract.getTimespan(Arrays.asList(t));

        assertEquals(t.getTimestamp(), span.getStart());
        assertEquals(t.getTimestamp(), span.getEnd());
    }

    @Test
    public void testGetTimespanChronologicalOrder() {
        Tweet t1 = makeTweet("alice", "First",
                "2025-01-01T10:00:00Z");
        Tweet t2 = makeTweet("bob", "Second",
                "2025-01-01T11:00:00Z");
        Tweet t3 = makeTweet("carol", "Third",
                "2025-01-01T12:00:00Z");

        Timespan span = Extract.getTimespan(
                Arrays.asList(t1, t2, t3));

        assertEquals(t1.getTimestamp(), span.getStart());
        assertEquals(t3.getTimestamp(), span.getEnd());
    }

    @Test
    public void testGetTimespanReverseOrder() {
        Tweet latest = makeTweet("alice", "Latest",
                "2025-01-01T12:00:00Z");
        Tweet earliest = makeTweet("bob", "Earliest",
                "2025-01-01T09:00:00Z");

        Timespan span = Extract.getTimespan(
                Arrays.asList(latest, earliest));

        assertEquals(earliest.getTimestamp(), span.getStart());
        assertEquals(latest.getTimestamp(), span.getEnd());
    }

    @Test
    public void testGetTimespanMixedOrder() {
        Tweet t1 = makeTweet("alice", "A",
                "2025-01-01T11:00:00Z");
        Tweet t2 = makeTweet("bob", "B",
                "2025-01-01T09:00:00Z");
        Tweet t3 = makeTweet("carol", "C",
                "2025-01-01T13:00:00Z");
        Tweet t4 = makeTweet("dave", "D",
                "2025-01-01T10:00:00Z");

        Timespan span = Extract.getTimespan(
                Arrays.asList(t1, t2, t3, t4));

        assertEquals(t2.getTimestamp(), span.getStart());
        assertEquals(t3.getTimestamp(), span.getEnd());
    }

    @Test
    public void testGetTimespanIdenticalTimestamps() {
        Instant time = Instant.parse("2025-01-01T10:00:00Z");

        Tweet t1 = new Tweet(1L, "alice", "A", time);
        Tweet t2 = new Tweet(2L, "bob", "B", time);

        Timespan span = Extract.getTimespan(Arrays.asList(t1, t2));

        assertEquals(time, span.getStart());
        assertEquals(time, span.getEnd());
    }

    // ---------------- getMentionedUsers tests ----------------

    @Test
    public void testGetMentionedUsersNone() {
        Tweet t = makeTweet("alice", "Hello everyone",
                "2025-01-01T10:00:00Z");

        Set<String> users = Extract.getMentionedUsers(
                Arrays.asList(t));

        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetMentionedUsersOne() {
        Tweet t = makeTweet("alice", "Hello @bob",
                "2025-01-01T10:00:00Z");

        Set<String> users = Extract.getMentionedUsers(
                Arrays.asList(t));

        assertEquals(1, users.size());
        assertTrue(users.contains("bob"));
    }

    @Test
    public void testGetMentionedUsersMultiple() {
        Tweet t = makeTweet("alice", "Hello @bob and @carol",
                "2025-01-01T10:00:00Z");

        Set<String> users = Extract.getMentionedUsers(
                Arrays.asList(t));

        assertEquals(2, users.size());
        assertTrue(users.contains("bob"));
        assertTrue(users.contains("carol"));
    }

    @Test
    public void testGetMentionedUsersRepeated() {
        Tweet t = makeTweet("alice", "@bob says hi to @bob",
                "2025-01-01T10:00:00Z");

        Set<String> users = Extract.getMentionedUsers(
                Arrays.asList(t));

        assertEquals(1, users.size());
        assertTrue(users.contains("bob"));
    }

    @Test
    public void testGetMentionedUsersAcrossTweets() {
        Tweet t1 = makeTweet("alice", "Hello @bob",
                "2025-01-01T10:00:00Z");
        Tweet t2 = makeTweet("carol", "Hello @dave",
                "2025-01-01T11:00:00Z");

        Set<String> users = Extract.getMentionedUsers(
                Arrays.asList(t1, t2));

        assertEquals(2, users.size());
        assertTrue(users.contains("bob"));
        assertTrue(users.contains("dave"));
    }
}
