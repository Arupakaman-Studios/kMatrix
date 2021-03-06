package com.arupakaman.kmatrix.uiModules.other;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.arupakaman.kmatrix.utils.deque.ArrayDeque;
import com.arupakaman.kmatrix.constants.AppConstants;


public class BitSequenceMain {

    /** The Mask to use for blurred text */
    private static final BlurMaskFilter blurFilter = new BlurMaskFilter(3, Blur.NORMAL);

    /** The Mask to use for slightly blurred text */
    private static final BlurMaskFilter slightBlurFilter = new BlurMaskFilter(
            2, Blur.NORMAL);

    /** The Mask to use for regular text */
    private static final BlurMaskFilter regularFilter = null;

    /** The height of the screen */
    private static int HEIGHT;

    /** The bits this sequence stores */
    private ArrayDeque<String> bits = new ArrayDeque<>();

    /** A variable used for all operations needing random numbers */
    private Random r = new Random();

    /** The scheduled operation for changing a bit and shifting downwards */
    private ScheduledFuture<?> future;

    /** The position to draw the sequence at on the screen */
    float x, y;

    /** True when the BitSequence should be paused */
    private boolean pause = false;

    private static final ScheduledExecutorService scheduler = Executors
            .newSingleThreadScheduledExecutor();

    /** The characters to use in the sequence */
    private static String[] symbols = null;

    /** Describes the style of the sequence */
    private final Style style = new Style();
    private static String charSet;
    private static boolean isRandom = true;
    private int curChar = 0;

    public static class Style {
        /** The default speed at which bits should be changed */
        private static final int DEFAULT_CHANGE_BIT_SPEED = 100;

        /** The maximum alpha a bit can have */
        private static final int MAX_ALPHA = 240;

        private static int changeBitSpeed;
        private static int numBits;
        private static int color;
        private static int defaultTextSize;
        private static int defaultFallingSpeed;
        private static boolean depthEnabled;

        private static int alphaIncrement;
        private static int initialY;

        private int textSize;
        private int fallingSpeed;
        private BlurMaskFilter maskFilter;

        private Paint paint = new Paint();

        public static void initParameters(Context context) {
            String charSetName = BitSequenceUtil.getCharSetName(context);
            isRandom = true;
            switch (charSetName) {
                case AppConstants.CHAR_SET_NAME_BINARY:
                    charSet = AppConstants.CHAR_SET_VALUE_BINARY;
                    break;
                case AppConstants.CHAR_SET_NAME_MATRIX:
                    charSet = AppConstants.CHAR_SET_VALUE_MATRIX;
                    break;
                case AppConstants.CHAR_SET_NAME_CUSTOM_RANDOM:
                    charSet = BitSequenceUtil.getCharSetCustomRandomName(context);
                    if (charSet.length() == 0) charSet = "kMatrix";
                    break;
                case AppConstants.CHAR_SET_NAME_CUSTOM_EXACT:
                    isRandom = false;
                    charSet = BitSequenceUtil.getCharSetCustomExactName(context);
                    if (charSet.length() == 0) charSet = "kMatrix";
                    break;
                default:
                    charSet = "kMatrix";
                    break;
            }
            symbols = charSet.split("(?!^)");

            if (isRandom) {
                numBits = BitSequenceUtil.getNumBits(context);
            } else {
                numBits = charSet.length();
            }
            color = android.graphics.Color.parseColor(BitSequenceUtil.getColorBits(context));
            defaultTextSize = BitSequenceUtil.getSizeBits(context);

            double changeBitSpeedMultiplier = BitSequenceUtil.getChangeSpeedBits(context) / 100;
            double fallingSpeedMultiplier = BitSequenceUtil.getFallSpeedBits(context) / 100;

            changeBitSpeed = (int) (DEFAULT_CHANGE_BIT_SPEED * changeBitSpeedMultiplier);
            defaultFallingSpeed = (int) (defaultTextSize * fallingSpeedMultiplier);

            depthEnabled = BitSequenceUtil.getDepthEnabled(context);

            alphaIncrement = MAX_ALPHA / numBits;
            initialY = -1 * defaultTextSize * numBits;
        }

        public Style() {
            paint.setColor(color);
        }

        public void createPaint() {
            paint.setTextSize(textSize);
            paint.setMaskFilter(maskFilter);
        }

    }

    /**
     * Resets the sequence by repositioning it, resetting its visual
     * characteristics, and rescheduling the thread
     */
    private void reset() {
        y = Style.initialY;
        setDepth();
        style.createPaint();
        scheduleThread();
    }

    /**
     * A runnable that changes the bit, moves the sequence down, and reschedules
     * its execution
     */
    private final Runnable changeBitRunnable = new Runnable() {
        public void run() {
            changeBit();
            y += style.fallingSpeed;
            if (y > HEIGHT) {
                reset();
            }
        }
    };

    private void setDepth() {
        if (!Style.depthEnabled) {
            style.textSize = Style.defaultTextSize;
            style.fallingSpeed = Style.defaultFallingSpeed;
        } else {
            double factor = r.nextDouble() * (1 - .8) + .8;
            style.textSize = (int) (Style.defaultTextSize * factor);
            style.fallingSpeed = (int) (Style.defaultFallingSpeed * Math.pow(
                    factor, 4));

            if (factor > .93) {
                style.maskFilter = regularFilter;
            } else if (factor <= .93 && factor >= .87) {
                style.maskFilter = slightBlurFilter;
            } else {
                style.maskFilter = blurFilter;
            }
        }
    }

    /**
     * Configures any BitSequences parameters requiring the application context
     *
     * @param context
     *            the application context
     */
    public static void configure(Context context) {
        try {
            Style.initParameters(context);
        }catch (Exception e){
            Log.e("BitSequencesMain", "configure", e);
        }
    }

    /**
     * Configures the BitSequence based on the display
     *
     * @param width
     *            the width of the screen
     * @param height
     *            the height of the screen
     */
    public static void setScreenDim(int width, int height) {
        HEIGHT = height;
    }

    public BitSequenceMain(int x) {
        curChar = 0;
        for (int i = 0; i < Style.numBits; i++) {
            if (isRandom) {
                bits.add(getRandomBit(r));
            } else {
                // TODO: Disable numBits in settings if custom is selected
                bits.addFirst(getNextBit());
            }
        }
        this.x = x;
        reset();
    }

    /**
     * Pauses the BitSequence by cancelling the ScheduledFuture
     */
    public void pause() {
        if (!pause) {
            if (future != null) {
                future.cancel(true);
            }
            pause = true;
        }
    }

    public void stop() {
        pause();
    }

    /**
     * Unpauses the BitSequence by scheduling BitSequences on the screen to
     * immediately start, and scheduling BitSequences off the screen to start
     * after some delay
     */
    public void unpause() {
        if (pause) {
            if (y <= Style.initialY + style.textSize || y > HEIGHT) {
                scheduleThread();
            } else {
                scheduleThread(0);
            }
            pause = false;
        }
    }

    /**
     * Schedules the changeBitRunnable with a random delay less than 6000
     * milliseconds, cancelling the previous scheduled future
     */
    private void scheduleThread() {
        scheduleThread(r.nextInt(6000));
    }

    /**
     * Schedules the changeBitRunnable with the specified delay, cancelling the
     * previous scheduled future
     *
     * @param delay
     *            the delay in milliseconds
     */

    private int retryCount = 0;
    private void scheduleThread(int delay) {
        try{
            if (future != null)
                future.cancel(true);
            future = scheduler.scheduleAtFixedRate(changeBitRunnable, delay, Style.changeBitSpeed, TimeUnit.MILLISECONDS);
            retryCount = 0;
        }catch (Exception e){
            if (retryCount < 3)
                scheduleThread();
            retryCount++;
            Log.e("BitSequencesMain", "scheduleThread", e);
        }
    }

    /** Shifts the bits back by one and adds a new bit to the end */
    synchronized private void changeBit() {
        if (isRandom) {
            bits.removeFirst();
            bits.addLast(getRandomBit(r));
        }
    }

    private String getNextBit() {
        String s = Character.toString(charSet.charAt(curChar));
        curChar = (curChar + 1) % charSet.length();
        return s;
    }

    /**
     * Gets a new random bit
     *
     * @param r
     *            the {@link Random} object to use
     * @return A new random bit as a {@link String}
     */
    private String getRandomBit(Random r) {
        return symbols[r.nextInt(symbols.length)];
    }

    /**
     * Gets the width the BitSequence would be on the screen
     *
     * @return the width of the BitSequence
     */
    public static float getWidth() {
        Paint paint = new Paint();
        paint.setTextSize(Style.defaultTextSize);
        return paint.measureText("0");
    }

    /**
     * Draws this BitSequence on the screen
     *
     * @param canvas
     *            the {@link Canvas} on which to draw the BitSequence
     */
    synchronized public void draw(Canvas canvas) {
        // TODO Can the get and set alphas be optimized?
        Paint paint = style.paint;
        float bitY = y;
        paint.setAlpha(Style.alphaIncrement);
        for (int i = 0; i < bits.size(); i++) {
            canvas.drawText(bits.get(i), x, bitY, paint);
            bitY += style.textSize;
            paint.setAlpha(paint.getAlpha() + Style.alphaIncrement);
        }
    }
}

