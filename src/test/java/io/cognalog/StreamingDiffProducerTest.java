package io.cognalog;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Tests for the {@link StreamingDiffProducer}.
 */
public class StreamingDiffProducerTest {
    private StreamingDiffProducer<Integer> subject = new StreamingDiffProducer<>();

    @Test
    public void testEmptyEmpty() throws Exception {
        final List<Integer> list1 = Collections.emptyList();
        final List<Integer> list2 = Collections.emptyList();
        final DiffFacts<Integer> result = subject.diff(list1, list2, Integer::compareTo);
        Assert.assertEquals(result.getInsertions(), Collections.emptyList());
        Assert.assertEquals(result.getDeletions(), Collections.emptyList());
    }
}