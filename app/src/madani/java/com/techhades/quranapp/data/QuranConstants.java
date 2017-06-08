package com.techhades.quranapp.data;

import android.support.annotation.NonNull;
import android.view.Display;

import com.techhades.quranapp.util.QuranScreenInfo;

public class QuranConstants {
  public static final int NUMBER_OF_PAGES = 604;

  public static QuranScreenInfo.PageProvider getPageProvider(@NonNull Display display) {
    return new QuranScreenInfo.DefaultPageProvider(display);
  }
}
