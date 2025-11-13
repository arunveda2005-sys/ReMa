package com.example.receipematcher.ui.theme;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

public class Type{

    // Body Large Style
    public static class BodyLarge {
        public static final float FONT_SIZE_SP = 16f;
        public static final float LINE_HEIGHT_SP = 24f;
        public static final float LETTER_SPACING_SP = 0.5f;
        public static final int FONT_WEIGHT = Typeface.NORMAL;
    }

    // Title Large Style
    public static class TitleLarge {
        public static final float FONT_SIZE_SP = 22f;
        public static final float LINE_HEIGHT_SP = 28f;
        public static final float LETTER_SPACING_SP = 0f;
        public static final int FONT_WEIGHT = Typeface.NORMAL;
    }

    // Label Small Style
    public static class LabelSmall {
        public static final float FONT_SIZE_SP = 11f;
        public static final float LINE_HEIGHT_SP = 16f;
        public static final float LETTER_SPACING_SP = 0.5f;
        public static final int FONT_WEIGHT = Typeface.BOLD; // Medium weight approximated as BOLD
    }

    // Headline Medium Style
    public static class HeadlineMedium {
        public static final float FONT_SIZE_SP = 18f;
        public static final float LINE_HEIGHT_SP = 26f;
        public static final float LETTER_SPACING_SP = 0f;
        public static final int FONT_WEIGHT = Typeface.NORMAL;
    }

    // Body Medium Style
    public static class BodyMedium {
        public static final float FONT_SIZE_SP = 14f;
        public static final float LINE_HEIGHT_SP = 20f;
        public static final float LETTER_SPACING_SP = 0.25f;
        public static final int FONT_WEIGHT = Typeface.NORMAL;
    }

    /**
     * Apply Body Large style to a TextView
     */
    public static void applyBodyLarge(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BodyLarge.FONT_SIZE_SP);
        textView.setLetterSpacing(BodyLarge.LETTER_SPACING_SP / BodyLarge.FONT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT, BodyLarge.FONT_WEIGHT);
        textView.setLineSpacing(0, BodyLarge.LINE_HEIGHT_SP / BodyLarge.FONT_SIZE_SP);
    }

    /**
     * Apply Title Large style to a TextView
     */
    public static void applyTitleLarge(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TitleLarge.FONT_SIZE_SP);
        textView.setLetterSpacing(TitleLarge.LETTER_SPACING_SP / TitleLarge.FONT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT, TitleLarge.FONT_WEIGHT);
        textView.setLineSpacing(0, TitleLarge.LINE_HEIGHT_SP / TitleLarge.FONT_SIZE_SP);
    }

    /**
     * Apply Label Small style to a TextView
     */
    public static void applyLabelSmall(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LabelSmall.FONT_SIZE_SP);
        textView.setLetterSpacing(LabelSmall.LETTER_SPACING_SP / LabelSmall.FONT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT, LabelSmall.FONT_WEIGHT);
        textView.setLineSpacing(0, LabelSmall.LINE_HEIGHT_SP / LabelSmall.FONT_SIZE_SP);
    }

    /**
     * Apply Headline Medium style to a TextView
     */
    public static void applyHeadlineMedium(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, HeadlineMedium.FONT_SIZE_SP);
        textView.setLetterSpacing(HeadlineMedium.LETTER_SPACING_SP / HeadlineMedium.FONT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT, HeadlineMedium.FONT_WEIGHT);
        textView.setLineSpacing(0, HeadlineMedium.LINE_HEIGHT_SP / HeadlineMedium.FONT_SIZE_SP);
    }

    /**
     * Apply Body Medium style to a TextView
     */
    public static void applyBodyMedium(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BodyMedium.FONT_SIZE_SP);
        textView.setLetterSpacing(BodyMedium.LETTER_SPACING_SP / BodyMedium.FONT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT, BodyMedium.FONT_WEIGHT);
        textView.setLineSpacing(0, BodyMedium.LINE_HEIGHT_SP / BodyMedium.FONT_SIZE_SP);
    }

    /**
     * Get text size in pixels for a given SP value
     */
    public static float spToPx(Context context, float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Apply custom text style to a TextView
     */
    public static void applyCustomStyle(TextView textView, float fontSizeSp,
                                        float letterSpacingSp, int fontWeight) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
        textView.setLetterSpacing(letterSpacingSp / fontSizeSp);
        textView.setTypeface(Typeface.DEFAULT, fontWeight);
    }

    // Private constructor to prevent instantiation
    private Type() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}