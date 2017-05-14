package de.seemoo.nexmon.jammer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;


/**
 * Created by stathis on 5/15/17.
 */

public class VerticalTextView extends android.support.v7.widget.AppCompatTextView {

    public VerticalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ColorStateList csl = getTextColors();
        final int color = csl.getDefaultColor();
        final int paddingBottom = getPaddingBottom();
        final int paddingTop = getPaddingTop();
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();
        final TextPaint paint = getPaint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(color);
        final float bottom = viewWidth * 9.0f / 11.0f;
        Path p = new Path();
        p.moveTo(bottom, viewHeight - paddingBottom - paddingTop);
        p.lineTo(bottom, paddingTop);
        canvas.drawTextOnPath(getText().toString(), p, 0, 0, paint);
    }
}