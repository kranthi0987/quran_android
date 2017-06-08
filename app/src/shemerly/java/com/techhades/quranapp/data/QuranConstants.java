package com.techhades.quranapp.data;

import com.techhades.quranapp.util.QuranScreenInfo;
import com.techhades.quranapp.util.ShemerlyPageProvider;

import android.support.annotation.NonNull;
import android.view.Display;

public class QuranConstants {
  public static final int NUMBER_OF_PAGES = 521;

  public static QuranScreenInfo.PageProvider getPageProvider(@NonNull Display display) {
    return new ShemerlyPageProvider();
  }
}
