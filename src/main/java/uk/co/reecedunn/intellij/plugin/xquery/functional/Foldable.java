/*
 * Copyright (C) 2016 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xquery.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface Foldable<A> {
    <V> V fold(BiFunction<A, V, V> foldOver, V initialValue);

    default <V> V foldIf(Predicate<A> matcher, BiFunction<A, V, V> foldOver, V initialValue) {
        return fold((a, b) -> {
            if (matcher.test(a)) {
                return foldOver.apply(a, b);
            }
            return b;
        }, initialValue);
    }

    default <V> V foldIf(Class c, BiFunction<A, V, V> foldOver, V initialValue) {
        return foldIf(c::isInstance, foldOver, initialValue);
    }

    default int count() {
        return fold((a, value) -> value + 1, 0);
    }

    default int countOf(Predicate<A> matcher) {
        return foldIf(matcher, (a, value) -> value + 1, 0);
    }

    default int countOf(Class c) {
        return foldIf(c::isInstance, (a, value) -> value + 1, 0);
    }

    default List<A> toList() {
        return fold((a, c) -> { c.add(a); return c; }, new ArrayList<A>());
    }
}
