package com.techhades.quranapp.presenter.quran.ayahtracker;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.techhades.quranapp.common.AyahBounds;
import com.techhades.quranapp.dao.Bookmark;
import com.techhades.quranapp.data.SuraAyah;
import com.techhades.quranapp.ui.helpers.HighlightType;
import com.techhades.quranapp.widgets.AyahToolBar;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AyahTrackerItem<T> {
  final int page;
  @NonNull
  final T ayahView;

  AyahTrackerItem(int page, @NonNull T ayahView) {
    this.page = page;
    this.ayahView = ayahView;
  }

  void onSetPageBounds(int page, @NonNull RectF bounds) {
  }

  void onSetAyahCoordinates(int page, @NonNull Map<String, List<AyahBounds>> coordinates) {
  }

  void onSetAyahBookmarks(@NonNull List<Bookmark> bookmarks) {
  }

  boolean onHighlightAyah(int page, int sura, int ayah, HighlightType type, boolean scrollToAyah) {
    return false;
  }

  void onHighlightAyat(int page, Set<String> ayahKeys, HighlightType type) {
  }

  void onUnHighlightAyah(int page, int sura, int ayah, HighlightType type) {
  }

  void onUnHighlightAyahType(HighlightType type) {
  }

  @Nullable
  AyahToolBar.AyahToolBarPosition getToolBarPosition(int page, int sura, int ayah,
                                                     int toolBarWidth, int toolBarHeight) {
    return null;
  }

  @Nullable
  SuraAyah getAyahForPosition(int page, float x, float y) {
    return null;
  }
}
