package com.techhades.quranapp.component.fragment;

import com.techhades.quranapp.di.QuranPageScope;
import com.techhades.quranapp.module.fragment.QuranPageModule;
import com.techhades.quranapp.ui.fragment.QuranPageFragment;
import com.techhades.quranapp.ui.fragment.TabletFragment;
import com.techhades.quranapp.ui.fragment.TranslationFragment;

import dagger.Subcomponent;

@QuranPageScope
@Subcomponent(modules = QuranPageModule.class)
public interface QuranPageComponent {
  void inject(QuranPageFragment quranPageFragment);

  void inject(TabletFragment tabletFragment);

  void inject(TranslationFragment translationFragment);

  @Subcomponent.Builder
  interface Builder {
    Builder withQuranPageModule(QuranPageModule quranPageModule);

    QuranPageComponent build();
  }
}
