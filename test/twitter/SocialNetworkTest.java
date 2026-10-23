package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy:
     *
     * guessFollowsGraph:
     * Partition by number of tweets (0, 1, >1).
     * Partition by mentions in tweets:
     *   - no mentions in tweets
     *   - single mention in single tweet
     *   - multiple mentions in single tweet
     *   - multiple tweets from the same user with overlapping and distinct mentions
     *   - case-insensitivity of usernames
     *   - author mentioning themselves (self-mentions should not result in self-following)
     *
     * influencers:
     * Partition by followsGraph:
     *   - empty graph
     *   - graph with a user who has no followers (follower count = 0)
     *   - graph with single influencer (one followed user)
     *   - graph with multiple influencers (descending follower counts)
     *   - graph with tied influence (equal number of followers)
     *
     * Use small artificial tweets with known authors, text, and timestamps.
     */

    private static long idCounter = 1L;

    private Tweet makeTweet(String author, String text) {
        return new Tweet(idCounter++, author, text, Instant.parse("2026-10-23T10:00:00Z"));
    }

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ---------------- guessFollowsGraph tests ----------------

    @Test
    public void testEmptyListOfTweets() {
        // Ensures that an empty list results in an empty graph.
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Collections.emptyList());
        assertTrue("expected empty graph for empty list of tweets", followsGraph.isEmpty());
    }

    @Test
    public void testTweetsWithoutMentions() {
        // Verifies that tweets with no mentions do not add entries to the graph.
        Tweet t1 = makeTweet("alice", "Enjoying a calm morning coffee");
        Tweet t2 = makeTweet("bob", "Programming in Java is productive");
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2));
        assertTrue("expected empty graph when tweets have no mentions", followsGraph.isEmpty());
    }

    @Test
    public void testSingleMention() {
        // Tests whether a user who mentions someone is correctly added to the graph.
        Tweet t = makeTweet("alice", "Hello @bob, hope you are doing well!");
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("expected alice to be in the graph", followsGraph.containsKey("alice"));
        assertEquals("expected alice to follow bob", Collections.singleton("bob"), followsGraph.get("alice"));
    }

    @Test
    public void testMultipleMentions() {
        // Checks if multiple mentioned users are linked to the tweet author.
        Tweet t = makeTweet("alice", "Great meeting with @bob and @carol today!");
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("expected alice to be in the graph", followsGraph.containsKey("alice"));
        Set<String> expectedFollows = new HashSet<>(Arrays.asList("bob", "carol"));
        assertEquals("expected alice to follow bob and carol", expectedFollows, followsGraph.get("alice"));
    }

    @Test
    public void testMultipleTweetsFromOneUser() {
        // Ensures that repeated mentions from the same user are captured.
        Tweet t1 = makeTweet("alice", "Hello @bob");
        Tweet t2 = makeTweet("alice", "Meeting @carol and @bob again");
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2));

        assertTrue("expected alice to be in the graph", followsGraph.containsKey("alice"));
        Set<String> expectedFollows = new HashSet<>(Arrays.asList("bob", "carol"));
        assertEquals("expected combined mentions from multiple tweets", expectedFollows, followsGraph.get("alice"));
    }

    // ---------------- influencers tests ----------------

    @Test
    public void testEmptyGraphForInfluencers() {
        // Verifies that no users yield an empty influencer list.
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> result = SocialNetwork.influencers(followsGraph);
        assertTrue("expected empty list for empty graph", result.isEmpty());
    }

    @Test
    public void testSingleUserWithoutFollowers() {
        // Tests that a user without followers yields no influencers.
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>());
        List<String> result = SocialNetwork.influencers(followsGraph);
        assertTrue("expected empty influencer list for user without followers", result.isEmpty());
    }

    @Test
    public void testSingleInfluencer() {
        // Verifies correct identification of the only influencer.
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Collections.singletonList("bob")));
        List<String> result = SocialNetwork.influencers(followsGraph);
        assertEquals("expected bob as the only influencer", Collections.singletonList("bob"), result);
    }

    @Test
    public void testMultipleInfluencers() {
        // Tests for correct influencer ordering.
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Arrays.asList("bob", "carol")));
        followsGraph.put("dave", new HashSet<>(Collections.singletonList("bob")));
        List<String> result = SocialNetwork.influencers(followsGraph);

        // bob has 2 followers (alice, dave); carol has 1 follower (alice)
        assertEquals("expected bob before carol", Arrays.asList("bob", "carol"), result);
    }

    @Test
    public void testTiedInfluence() {
        // Ensures equal influencers are handled correctly.
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Collections.singletonList("bob")));
        followsGraph.put("carol", new HashSet<>(Collections.singletonList("dave")));
        List<String> result = SocialNetwork.influencers(followsGraph);

        assertEquals("expected 2 influencers in total", 2, result.size());
        assertTrue("expected list to contain bob", result.contains("bob"));
        assertTrue("expected list to contain dave", result.contains("dave"));
    }

}
