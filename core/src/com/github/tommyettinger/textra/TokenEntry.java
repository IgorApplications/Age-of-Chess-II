/*
 * Copyright (c) 2021-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra;

/**
 * Container representing a token, parsed parameters and its position in text.
 */
class TokenEntry implements Comparable<TokenEntry> {
    String token;
    TokenCategory category;
    int index;
    int endIndex;
    float floatValue;
    String stringValue;
    Effect effect;

    TokenEntry(String token, TokenCategory category, int index, int endIndex, float floatValue, String stringValue) {
        this.token = token;
        this.category = category;
        this.index = index;
        this.endIndex = endIndex;
        this.floatValue = floatValue;
        this.stringValue = stringValue;
    }

    @Override
    public int compareTo(TokenEntry o) {
        return Integer.compare(o.index, index);
    }

}
