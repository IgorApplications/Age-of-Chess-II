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

package com.github.tommyettinger.textra.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Shakes the text in a random pattern.
 */
public class ShakeEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.12f;
    private static final float DEFAULT_INTENSITY = 0.5f;

    private final FloatArray lastOffsets = new FloatArray();

    private float distance = 1; // How far the glyphs should move
    private float intensity = 1; // How fast the glyphs should move

    public ShakeEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Intensity
        if (params.length > 1) {
            this.intensity = paramAsFloat(params[1], 1);
        }

        // Duration
        if (params.length > 2) {
            this.duration = paramAsFloat(params[2], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Make sure we can hold enough entries for the current index
        if (localIndex >= lastOffsets.size / 2) {
            lastOffsets.setSize(lastOffsets.size + 16);
        }

        // Get last offsets
        float lastX = lastOffsets.get(localIndex * 2);
        float lastY = lastOffsets.get(localIndex * 2 + 1);

        // Calculate new offsets
        float x = label.getLineHeight(globalIndex) * distance * MathUtils.random(-1f, 1f) * DEFAULT_DISTANCE;
        float y = label.getLineHeight(globalIndex) * distance * MathUtils.random(-1f, 1f) * DEFAULT_DISTANCE;

        // Apply intensity
        float normalIntensity = MathUtils.clamp(intensity * DEFAULT_INTENSITY, 0, 1);
        x = Interpolation.linear.apply(lastX, x, normalIntensity);
        y = Interpolation.linear.apply(lastY, y, normalIntensity);

        // Apply fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;
        x = MathUtils.round(x);
        y = MathUtils.round(y);

        // Store offsets for the next tick
        lastOffsets.set(localIndex * 2, x);
        lastOffsets.set(localIndex * 2 + 1, y);

        // Apply changes
        label.offsets.incr(globalIndex << 1, x);
        label.offsets.incr(globalIndex << 1 | 1, y);
    }

}
