/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.util.Collections;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class MergeIteratorTest {

    @Test
    public void testMerging() {
        Iterator<Integer> it1 = Arrays.asList(1, 3, 5, 7).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();

        MergeIterator<Integer> mergeIterator = new MergeIterator<>(it1, it2, Comparator.naturalOrder());

        ArrayList<Integer> res = new ArrayList<>();
        while (mergeIterator.hasNext()) {
            res.add(mergeIterator.next());
        }

        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7), res);
    }

    @Test
    public void testWithEmptyIterator1() {
        Iterator<Integer> it1 = Collections.<Integer> emptyList().iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();

        MergeIterator<Integer> mergeIterator = new MergeIterator<>(it1, it2, Comparator.naturalOrder());

        ArrayList<Integer> res = new ArrayList<>();
        while (mergeIterator.hasNext()) {
            res.add(mergeIterator.next());
        }

        assertEquals(Arrays.asList(2, 4, 6), res);
    }

    @Test
    public void testWithEmptyIterator2() {
        Iterator<Integer> it1 = Arrays.asList(1, 3, 5, 7).iterator();
        Iterator<Integer> it2 = Collections.<Integer> emptyList().iterator();

        MergeIterator<Integer> mergeIterator = new MergeIterator<>(it1, it2, Comparator.naturalOrder());

        ArrayList<Integer> res = new ArrayList<>();
        while (mergeIterator.hasNext()) {
            res.add(mergeIterator.next());
        }

        assertEquals(Arrays.asList(1, 3, 5, 7), res);
    }

    @Test
    public void testWithBothEmptyIterators() {
        Iterator<Integer> it1 = Collections.<Integer> emptyList().iterator();
        Iterator<Integer> it2 = Collections.<Integer> emptyList().iterator();

        MergeIterator<Integer> mergeIterator = new MergeIterator<>(it1, it2, Comparator.naturalOrder());

        assertFalse(mergeIterator.hasNext());
    }

}
