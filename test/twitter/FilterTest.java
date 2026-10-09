
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy:
     *
     * writtenBy:
     * Test all matches, partial matches, no matches,
     * and an empty tweet list.
     *
     * inTimespan:
     * Test timestamps inside and outside the interval,
     * at the start and end boundaries, and mixed inputs.
     *
     * containing:
     * Test one word, multiple words, no matches,
     * and case differences.
     *
     * Use small artificial inputs for each test.
     */

    private Tweet makeTweet(String author, String text, String time) {
        return new Tweet(1L, author, text, Instant.parse(time));
    }

    // ---------------- writtenBy tests ----------------

    @Test
    public void testWrittenByAllMatch() {
        Tweet t1 = makeTweet("alice", "First",
                "2025-01-01T10:00:00Z");
        Tweet t2 = makeTweet("alice", "Second",
                "2025-01-01T11:00:00Z");

        List<Tweet> result = Filter.writtenBy(
                Arrays.asList(t1, t2), "alice");

        assertEquals(Arrays.asList(t1, t2), result);
    }

    @Test
    public void testWrittenByPartialMatch() {
        Tweet t1 = makeTweet("alice", "First",
                "2025-01-01T10:00:00Z");
        Tweet t2 = makeTweet("bob", "Second",
                "2025-01-01T11:00:00Z");
        Tweet t3 = makeTweet("alice", "Third",
                "2025-01-01T12:00:00Z");

        List<Tweet> result = Filter.writtenBy(
                Arrays.asList(t1, t2, t3), "alice");

        assertEquals(Arrays.asList(t1, t3), result);
    }

    @Test
    public void testWrittenByNoMatch() {
        Tweet t = makeTweet("alice", "Hello",
                "2025-01-01T10:00:00Z");

        List<Tweet> result = Filter.writtenBy(
                Arrays.asList(t), "bob");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testWrittenByEmptyInput() {
        List<Tweet> result = Filter.writtenBy(
                Collections.emptyList(), "alice");

        assertTrue(result.isEmpty());
    }

    // ---------------- inTimespan tests ----------------

    @Test
    public void testInTimespanInside() {
        Tweet t = makeTweet("alice", "Inside",
                "2025-01-01T11:00:00Z");

        Timespan span = new Timespan(
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T12:00:00Z"));

        List<Tweet> result = Filter.inTimespan(
                Arrays.asList(t), span);

        assertEquals(Arrays.asList(t), result);
    }

    @Test
    public void testInTimespanOutside() {
        Tweet t = makeTweet("alice", "Outside",
                "2025-01-01T13:00:00Z");

        Timespan span = new Timespan(
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T12:00:00Z"));

        List<Tweet> result = Filter.inTimespan(
                Arrays.asList(t), span);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testInTimespanStartBoundary() {
        Instant start = Instant.parse("2025-01-01T10:00:00Z");
        Tweet t = new Tweet(1L,"alice", "At start", start);

        Timespan span = new Timespan(
                start, Instant.parse("2025-01-01T12:00:00Z"));

        List<Tweet> result = Filter.inTimespan(
                Arrays.asList(t), span);

        assertEquals(Arrays.asList(t), result);
    }

    @Test
    public void testInTimespanEndBoundary() {
        Instant end = Instant.parse("2025-01-01T12:00:00Z");
        Tweet t = new Tweet(2L,"alice", "At end", end);

        Timespan span = new Timespan(
                Instant.parse("2025-01-01T10:00:00Z"), end);

        List<Tweet> result = Filter.inTimespan(
                Arrays.asList(t), span);

        assertEquals(Arrays.asList(t), result);
    }

    @Test
    public void testInTimespanMixedInput() {
        Tweet before = makeTweet("alice", "Before",
                "2025-01-01T09:00:00Z");
        Tweet inside = makeTweet("bob", "Inside",
                "2025-01-01T11:00:00Z");
        Tweet after = makeTweet("carol", "After",
                "2025-01-01T13:00:00Z");

        Timespan span = new Timespan(
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T12:00:00Z"));

        List<Tweet> result = Filter.inTimespan(
                Arrays.asList(before, inside, after), span);

        assertEquals(Arrays.asList(inside), result);
    }

    // ---------------- containing tests ----------------

    @Test
    public void testContainingOneWord() {
        Tweet t1 = makeTweet("alice", "Java is useful",
                "2025-01-01T10:00:00Z");
        Tweet t2 = makeTweet("bob", "Python is useful",
                "2025-01-01T11:00:00Z");

        List<Tweet> result = Filter.containing(
                Arrays.asList(t1, t2), Arrays.asList("Java"));

        assertEquals(Arrays.asList(t1), result);
    }

    @Test
    public void testContainingMultipleWords() {
        Tweet t1 = makeTweet("alice", "Java is useful",
                "2025-01-01T10:00:00Z");
        Tweet t2 = makeTweet("bob", "Java is popular",
                "2025-01-01T11:00:00Z");
        Tweet t3 = makeTweet("carol", "Python is useful",
                "2025-01-01T12:00:00Z");

        List<Tweet> result = Filter.containing(
                Arrays.asList(t1, t2, t3),
                Arrays.asList("Java", "useful"));

        assertEquals(Arrays.asList(t1), result);
    }

    @Test
    public void testContainingNoMatches() {
        Tweet t = makeTweet("alice", "Java is useful",
                "2025-01-01T10:00:00Z");

        List<Tweet> result = Filter.containing(
                Arrays.asList(t), Arrays.asList("Rust"));

        assertTrue(result.isEmpty());
    }

    @Test
    public void testContainingCaseInsensitive() {
        Tweet t = makeTweet("alice", "JAVA is useful",
                "2025-01-01T10:00:00Z");

        List<Tweet> result = Filter.containing(
                Arrays.asList(t), Arrays.asList("java"));

        assertEquals(Arrays.asList(t), result);
    }

    @Test
    public void testContainingEmptyTweetList() {
        List<Tweet> result = Filter.containing(
                Collections.emptyList(), Arrays.asList("Java"));

        assertTrue(result.isEmpty());
    }
}
