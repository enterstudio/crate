/*
 * Licensed to Crate.io Inc. (Crate) under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file to
 * you under the Apache License, Version 2.0 (the "License");  you may not
 * use this file except in compliance with the License.  You may obtain a
 * copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, to use any modules in this file marked as "Enterprise Features",
 * Crate must have given you permission to enable and use such Enterprise
 * Features and you must have a valid Enterprise or Subscription Agreement
 * with Crate.  If you enable or use the Enterprise Features, you represent
 * and warrant that you have a valid Enterprise or Subscription Agreement
 * with Crate.  Your use of the Enterprise Features if governed by the terms
 * and conditions of your Enterprise or Subscription Agreement with Crate.
 */

package io.crate.lucene.match;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRefBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation tying Java's built-in java.util.regex to {@link CrateRegexQuery}.
 */
public final class CrateRegexCapabilities {

    public static final int FLAG_UNICODE_CASE = Pattern.UNICODE_CASE;
    public static final int FLAG_CASE_INSENSITIVE = Pattern.CASE_INSENSITIVE;

    static JavaUtilRegexMatcher compile(String regex, int flags) {
        return new CrateRegexCapabilities.JavaUtilRegexMatcher(regex, flags);
    }

    static class JavaUtilRegexMatcher {
        private final Pattern pattern;
        private final Matcher matcher;
        private final CharsRefBuilder utf16 = new CharsRefBuilder();

        JavaUtilRegexMatcher(String regex, int flags) {
            this.pattern = Pattern.compile(regex, flags);
            this.matcher = this.pattern.matcher(utf16.get());
        }

        boolean match(BytesRef term) {
            utf16.copyUTF8Bytes(term);
            utf16.get();
            return matcher.reset().matches();
        }
    }
}