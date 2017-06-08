package com.techhades.quranapp.component.activity;

import com.techhades.quranapp.component.fragment.QuranPageComponent;
import com.techhades.quranapp.di.ActivityScope;
import com.techhades.quranapp.module.activity.PagerActivityModule;
import com.techhades.quranapp.ui.PagerActivity;
import com.techhades.quranapp.ui.fragment.AyahTranslationFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = PagerActivityModule.class)
public interface PagerActivityComponent {
  // subcomponents
  QuranPageComponent.Builder quranPageComponentBuilder();

  void inject(PagerActivity pagerActivity);

  void inject(AyahTranslationFragment ayahTranslationFragment);

  @Subcomponent.Builder
  interface Builder {
    Builder withPagerActivityModule(PagerActivityModule pagerModule);

    PagerActivityComponent build();
  }
}
