/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.common;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UUIDCollisionTest {

    @Test
    @Ignore
    public void collisionWithFirstChars() {
        runWithFirstN(6, 1000);
        runWithFirstN(8, 1000);

        runWithFirstN(6, 1000000);
        runWithFirstN(8, 1000000);
    }

    private void runWithFirstN(int firstNChars, int loops) {
        Set<String> shortCollisions = new HashSet<>();
        Set<String> fullCollisions = new HashSet<>();
        int collided = 0;
        int fullCollided = 0;
        for (int i = 0; i < loops; i++) {
            String uuid = UUID.randomUUID().toString();
            boolean added = shortCollisions.add(uuid.substring(0, firstNChars));
            if (!added) {
                collided++;
            }
            boolean addedFull = fullCollisions.add(uuid);
            if (!addedFull) {
                fullCollided++;
            }
        }
        System.out.println("First " + firstNChars + " in " + loops + " loops:");
        float percent = (float) collided * 100 / loops;
        System.out.println("  Caused " + collided + " collisions or " + percent + "%");
        System.out.println("  Full UUID collided: " + fullCollided + " times.");
    }
}
