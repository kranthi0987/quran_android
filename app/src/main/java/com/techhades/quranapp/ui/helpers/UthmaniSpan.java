package com.techhades.quranapp.ui.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import com.techhades.quranapp.ui.util.TypefaceManager;

public class UthmaniSpan extends MetricAffectingSpan {
  private Typeface mTypeface;

  public UthmaniSpan(Context context) {
    mTypeface = TypefaceManager.getUthmaniTypeface(context);
  }

  @Override
  public void updateDrawState(TextPaint ds) {
    ds.setTypeface(mTypeface);
  }

  @Override
  public void updateMeasureState(TextPaint paint) {
    paint.setTypeface(mTypeface);
  }
}
