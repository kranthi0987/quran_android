package com.techhades.quranapp.module.activity;

import com.techhades.quranapp.di.ActivityScope;
import com.techhades.quranapp.ui.PagerActivity;
import com.techhades.quranapp.ui.helpers.AyahSelectedListener;
import com.techhades.quranapp.util.QuranScreenInfo;
import com.techhades.quranapp.util.QuranUtils;

import dagger.Module;
import dagger.Provides;

@Module
public class PagerActivityModule {
  private final PagerActivity pagerActivity;

  public PagerActivityModule(PagerActivity pagerActivity) {
    this.pagerActivity = pagerActivity;
  }

  @Provides
  AyahSelectedListener provideAyahSelectedListener() {
    return this.pagerActivity;
  }

  @Provides
  @ActivityScope
  String provideImageWidth(QuranScreenInfo screenInfo) {
    return QuranUtils.isDualPages(pagerActivity, screenInfo) ?
        screenInfo.getTabletWidthParam() : screenInfo.getWidthParam();
  }
}
