package io.cognalog;

import com.google.common.collect.Lists;
import io.cognalog.Snake.Direction;
import io.cognalog.Snake.Point;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Tyrone Hinderson 2014 Unit tests for {@link ListDiffUtil}
 */
public class MyersDiffProducerTest {
    private static List<String> s1 = Lists.newArrayList("a", "b", "c", "a", "d", "x", "b", "b", "a");
    private static List<String> s2 = Lists.newArrayList("a", "b", "a", "c", "a", "d", "b", "r", "b", "f", "z", "a");
    private static List<String> s3 = Collections.emptyList();

    /**
     * Make sure that a {@link Snake} list makes logical sense
     *
     * @param snakes the list of edit script steps
     * @throws Exception
     */
    private void verifySnakes(List<Snake> snakes, int n, int m) throws Exception {
        // make sure that the list of snakes covers the whole of the two sequences
        final Point expectedLastPoint = new Point(n, m);
        assertEquals(expectedLastPoint, snakes.get(snakes.size() - 1).getEnd());

        // make sure that the list of snakes truly represents an uninterrupted chain of steps through the sequences
        for (int i = 0; i < snakes.size(); i++) {
            final Snake snake1 = snakes.get(i);
            // diagonal moves depending on direction
            if (snake1.getDirection().equals(Direction.FORWARD)) {
                assertEquals(snake1.getEnd().getX() - snake1.getMid().getX(), snake1.getEnd().getY()
                        - snake1.getMid().getY());
            } else if (snake1.getDirection().equals(Direction.REVERSE)) {
                assertEquals(snake1.getMid().getX() - snake1.getStart().getX(), snake1.getMid().getY()
                        - snake1.getStart().getY());
            }
            // this end is the next start
            if (i < snakes.size() - 1) {
                assertEquals(snake1.getEnd(), snakes.get(i + 1).getStart());
            }
        }
    }

    @Test
    public void testNonemptyvsNonempty() throws Exception {
        final List<Snake> snakes = MyersDiffProducer.getSnakes(s1, s2, Integer.MAX_VALUE, String::compareTo);
        final Point expectedFirstPoint = new Point(0, 0);
        assertFalse(snakes.isEmpty());
        assertEquals(expectedFirstPoint, snakes.get(0).getStart());
        verifySnakes(snakes, s1.size(), s2.size());
    }

    @Test
    public void testEmptyVsNonempty() throws Exception {
        final List<Snake> snakes = MyersDiffProducer.getSnakes(s1, s3, Integer.MAX_VALUE, String::compareTo);
        final int expectedSnakeCount = s1.size();
        final Point expectedFirstPoint = new Point(0, 0);
        assertEquals(expectedSnakeCount, snakes.size());
        assertEquals(expectedFirstPoint, snakes.get(0).getStart());
        verifySnakes(snakes, s1.size(), s3.size());
    }

    @Test
    public void testEmptyvsEmpty() throws Exception {
        final List<Snake> snakes = MyersDiffProducer.getSnakes(s3, s3, Integer.MAX_VALUE, String::compareTo);
        assertTrue(snakes.isEmpty());
    }

    @Test
    public void testSameSequence() throws Exception {
        final List<Snake> snakes = MyersDiffProducer.getSnakes(s1, s1, Integer.MAX_VALUE, String::compareTo);
        final int expectedSnakeCount = 1;
        assertEquals(expectedSnakeCount, snakes.size());
        verifySnakes(snakes, s1.size(), s1.size());
    }

    private <T> void verifyDiffStringPositions(final List<T> s1, final List<T> s2, final DiffFacts<T> facts) {
        final String[] rows = facts.getDiffString().split("\n");
        for (String row : rows) {
            if (row.isEmpty()) {
                continue;
            }
            final List<T> listToCheck = row.charAt(0) == DiffFacts.DELETE_CHAR ? s1 : s2;
            final int lineNum = Integer.parseInt(row.substring(2, row.indexOf(' ', 2)));
            final String item = row.substring(row.indexOf(' ', 2) + 1);
            assertEquals(listToCheck.get(lineNum - 1).toString(), item);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSameSequenceFacts() throws Exception {
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>();
        DiffFacts<String> facts = subject.diff(s1, s1, String::compareTo);
        final int expectedInsertionCount = 0;
        final int expectedDeletionCount = 0;
        assertEquals(expectedInsertionCount, facts.getInsertions().size());
        assertEquals(expectedDeletionCount, facts.getDeletions().size());
        verifyDiffStringPositions(s1, s1, facts);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSameSequenceFactsLTD() throws Exception {
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>(0);
        DiffFacts<String> facts = subject.diff(s1, s1, String::compareTo);
        final int expectedInsertionCount = 0;
        final int expectedDeletionCount = 0;
        assertFalse(facts.isTruncated());
        assertEquals(expectedInsertionCount, facts.getInsertions().size());
        assertEquals(expectedDeletionCount, facts.getDeletions().size());
        verifyDiffStringPositions(s1, s1, facts);
    }

    @Test
    public void testEmptyVsEmptyFacts() throws Exception {
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>();
        final DiffFacts<String> facts = subject.diff(s3, s3, String::compareTo);
        final int expectedInsertionCount = 0;
        final int expectedDeletionCount = 0;
        assertEquals(expectedInsertionCount, facts.getInsertions().size());
        assertEquals(expectedDeletionCount, facts.getDeletions().size());
        verifyDiffStringPositions(s3, s3, facts);
    }

    @Test
    public void testEmptyVsEmptyFactsLTD() throws Exception {
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>(0);
        final DiffFacts<String> facts = subject.diff(s3, s3, String::compareTo);
        final int expectedInsertionCount = 0;
        final int expectedDeletionCount = 0;
        assertFalse(facts.isTruncated());
        assertEquals(expectedInsertionCount, facts.getInsertions().size());
        assertEquals(expectedDeletionCount, facts.getDeletions().size());
        verifyDiffStringPositions(s3, s3, facts);
    }

    @Test
    public void testEmptyVsNonemptyFacts() throws Exception {
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>();
        final DiffFacts<String> facts = subject.diff(s1, s3, String::compareTo);
        final int expectedInsertionCount = 0;
        final int expectedDeletionCount = s1.size();
        assertEquals(expectedInsertionCount, facts.getInsertions().size());
        assertEquals(expectedDeletionCount, facts.getDeletions().size());
        verifyDiffStringPositions(s1, s3, facts);
    }

    @Test
    public void testEmptyVsNonemptyFactsLTD() throws Exception {
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>(2);
        final DiffFacts<String> facts = subject.diff(s1, s3, String::compareTo);
        final int expectedInsertionCount = 0;
        final int expectedDeletionCount = 2;
        assertTrue(facts.isTruncated());
        assertEquals(expectedInsertionCount, facts.getInsertions().size());
        assertEquals(expectedDeletionCount, facts.getDeletions().size());
        verifyDiffStringPositions(s1, s3, facts);
    }

    @Test
    public void testNonemptyVsNonemptyFacts() throws Exception {
        // two different, nonempty sequences
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>();
        DiffFacts<String> facts = subject.diff(s1, s2, String::compareTo);
        final List<String> expectedInsertions = Lists.newArrayList("a", "r", "f", "z");
        final List<String> expectedDeletions = Lists.newArrayList("x");
        assertEquals(expectedInsertions, facts.getInsertions());
        assertEquals(expectedDeletions, facts.getDeletions());
        verifyDiffStringPositions(s1, s2, facts);
    }

    @Test
    public void testNonemptyVsNonemptyFactsLTD() throws Exception {
        // two different, nonempty sequences
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>(3);
        DiffFacts<String> facts = subject.diff(s1, s2, String::compareTo);
        final List<String> expectedInsertions = Lists.newArrayList("a", "r");
        final List<String> expectedDeletions = Lists.newArrayList("x");
        assertTrue(facts.isTruncated());
        assertEquals(expectedInsertions, facts.getInsertions());
        assertEquals(expectedDeletions, facts.getDeletions());
        verifyDiffStringPositions(s1, s2, facts);
    }

    @Test
    public void testNonemptyVsNonemptyFactsCustomComparator() throws Exception {
        // for this comparator, NO two Strings are equal.
        final MyersDiffProducer<String> subject = new MyersDiffProducer<>();
        final List<String> s1 = Lists.newArrayList("no match", "hey", "there", "hype man");
        final List<String> s2 = Lists.newArrayList("they", "therefore", "sorry pal", "man");
        final DiffFacts<String> facts = subject.diff(s1, s2, (o1, o2) -> {
            if (o1.contains(o2) || o2.contains(o1)) {
                return 0;
            }
            return 1;
        });
        final List<String> expectedInsertions = Collections.singletonList("sorry pal");
        final List<String> expectedDeletions = Collections.singletonList("no match");
        assertEquals(expectedInsertions, facts.getInsertions());
        assertEquals(expectedDeletions, facts.getDeletions());
        verifyDiffStringPositions(s1, s2, facts);
    }

    @Test
    public void testGetMiddleSnakeSame() throws Exception {
        final List<String> s1 = Lists.newArrayList("a", "b");
        final Snake middle = MyersDiffProducer.getMiddleSnake(s1, s1, String::compareTo);
        final Snake expectedMiddle = new Snake(new Point(0, 0), new Point(2, 2), new Point(2, 2), Direction.REVERSE, 0);
        assertEquals(expectedMiddle, middle);
    }

    @Test
    public void testGetMiddleSnakeDifferentSingleton() throws Exception {
        final List<String> s1 = Collections.singletonList("a");
        final List<String> s2 = Collections.singletonList("c");
        final Snake middle = MyersDiffProducer.getMiddleSnake(s1, s2, String::compareTo);
        final Snake expectedMiddle = new Snake(new Point(0, 1), new Point(0, 1), new Point(1, 1), Direction.REVERSE, 2);
        assertEquals(expectedMiddle, middle);
    }
}
