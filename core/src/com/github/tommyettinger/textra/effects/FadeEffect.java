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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.ColorUtils;

/**
 * Fades the text's color from between colors or alphas. Doesn't repeat itself.
 */
public class FadeEffect extends Effect {
    private int color1 = 256; // First color of the effect.
    private int color2 = 256; // Second color of the effect.
    private float alpha1 = 0; // First alpha of the effect, in case a color isn't provided.
    private float alpha2 = 1; // Second alpha of the effect, in case a color isn't provided.
    private float fadeDuration = 1; // Duration of the fade effect

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public FadeEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1 or Alpha 1
        if (params.length > 0) {
            this.color1 = paramAsColor(params[0]);
            if (this.color1 == 256) {
                alpha1 = paramAsFloat(params[0], 0);
            }
        }

        // Color 2 or Alpha 2
        if (params.length > 1) {
            this.color2 = paramAsColor(params[1]);
            if (this.color2 == 256) {
                alpha2 = paramAsFloat(params[1], 1);
            }
        }

        // Fade duration
        if (params.length > 2) {
            this.fadeDuration = paramAsFloat(params[2], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / fadeDuration, 0, 1);

        // Calculate initial color
        if (this.color1 == 256) {
            label.setInWorkingLayout(globalIndex,
                    glyph = (glyph & 0xFFFFFF00FFFFFFFFL) | (long) MathUtils.lerp(glyph >>> 32 & 255, this.alpha1 * 255, 1f - progress) << 32);
        } else {
            label.setInWorkingLayout(globalIndex,
                    glyph = (glyph & 0xFFFFFFFFL) | (long) ColorUtils.lerpColors((int) (glyph >>> 32), this.color1, 1f - progress) << 32);
        }

        // Calculate final color
        if (this.color2 == 256) {
            label.setInWorkingLayout(globalIndex,
                    (glyph & 0xFFFFFF00FFFFFFFFL) | (long) MathUtils.lerp(glyph >>> 32 & 255, this.alpha2 * 255, progress) << 32);
        } else {
            label.setInWorkingLayout(globalIndex,
                    (glyph & 0xFFFFFFFFL) | (long) ColorUtils.lerpColors((int) (glyph >>> 32), this.color2, progress) << 32);
        }
    }

}
