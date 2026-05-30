/*
 * Copyright (C) 2026 ZenithOS Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ---------------------------------------------------------------------------
 *
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.internal.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.HapticFeedbackConstants;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.Random;

/**
 * @hide
 */
public class PlatLogoActivity extends Activity {

    private PulsarView mPulsarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setNavigationBarColor(0);
        getWindow().setStatusBarColor(0);

        final ActionBar ab = getActionBar();
        if (ab != null) ab.hide();

        mPulsarView = new PulsarView(this);
        
        final FrameLayout layout = new FrameLayout(this);
        layout.setBackgroundColor(Color.BLACK);
        layout.addView(mPulsarView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        setContentView(layout);
    }

    private static class PulsarView extends View {
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final ArrayList<Star> mStars = new ArrayList<>();
        private final Random mRandom = new Random();
        
        private float mPulsarAngle = 0f;
        private float mSpinSpeed = 2f;
        private float mMaxSpinSpeed = 45f;
        private boolean mIsTouching = false;
        
        private boolean mShowText = false;
        private float mTextAlpha = 0f;
        
        private float mCx, mCy;

        public PulsarView(Context context) {
            super(context);
            for (int i = 0; i < 80; i++) {
                mStars.add(new Star());
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mCx = w / 2f;
            mCy = h / 2f;
            for (Star star : mStars) {
                star.reset(w, h, true);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            mPaint.setColor(Color.WHITE);
            for (Star star : mStars) {
                star.update(width, height, mSpinSpeed / 2f);
                mPaint.setAlpha(star.alpha);
                canvas.drawCircle(star.x, star.y, star.size, mPaint);
            }

            canvas.save();
            canvas.translate(mCx, mCy);
            canvas.rotate(mPulsarAngle);

            mPaint.setAlpha(255);
            mPaint.setColor(Color.parseColor("#00E5FF"));
            mPaint.setStrokeWidth(12f);
            canvas.drawLine(0, -Math.max(width, height), 0, Math.max(width, height), mPaint);

            mPaint.setColor(Color.parseColor("#9C27B0"));
            mPaint.setAlpha(100);
            mPaint.setStrokeWidth(30f);
            canvas.drawLine(0, -Math.max(width, height), 0, Math.max(width, height), mPaint);
            
            canvas.restore();

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            mPaint.setAlpha(255);
            canvas.drawCircle(mCx, mCy, 25f + (mSpinSpeed * 0.5f), mPaint);

            if (mShowText) {
                if (mTextAlpha < 255) mTextAlpha += 5;
                
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.setAlpha((int) mTextAlpha);
                
                mPaint.setTextSize(90f);
                mPaint.setColor(Color.WHITE);
                mPaint.setFakeBoldText(true);
                canvas.drawText("ZenithOS 4.0", mCx, mCy + 300f, mPaint);
                
                mPaint.setTextSize(65f);
                mPaint.setColor(Color.parseColor("#00E5FF"));
                mPaint.setFakeBoldText(false);
                canvas.drawText("Pulsar", mCx, mCy + 400f, mPaint);
            }

            if (mIsTouching) {
                if (mSpinSpeed < mMaxSpinSpeed) {
                    mSpinSpeed += 0.4f;
                    if ((int) mSpinSpeed % 5 == 0) {
                        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    }
                }
                if (mSpinSpeed >= mMaxSpinSpeed - 5f) {
                    mShowText = true;
                }
            } else {
                if (mSpinSpeed > 2f) {
                    mSpinSpeed -= 0.5f;
                } else {
                    mSpinSpeed = 2f;
                }
            }

            mPulsarAngle += mSpinSpeed;
            if (mPulsarAngle >= 360f) mPulsarAngle -= 360f;

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mIsTouching = true;
                    performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsTouching = false;
                    return true;
            }
            return super.onTouchEvent(event);
        }

        private class Star {
            float x, y;
            float size;
            float speed;
            int alpha;

            void reset(int width, int height, boolean startRandomly) {
                size = mRandom.nextFloat() * 5f + 2f;
                speed = mRandom.nextFloat() * 3f + 1f;
                alpha = mRandom.nextInt(155) + 100;

                if (startRandomly) {
                    x = mRandom.nextFloat() * width;
                    y = mRandom.nextFloat() * height;
                } else {
                    if (mRandom.nextBoolean()) {
                        x = mRandom.nextFloat() * width;
                        y = -10f;
                    } else {
                        x = -10f;
                        y = mRandom.nextFloat() * height;
                    }
                }
            }

            void update(int width, int height, float speedMultiplier) {
                x += speed * (1f + speedMultiplier * 0.2f);
                y += speed * (1f + speedMultiplier * 0.2f);

                if (x > width + 10 || y > height + 10) {
                    reset(width, height, false);
                }
            }
        }
    }
}