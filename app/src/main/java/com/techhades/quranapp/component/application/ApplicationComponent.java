package com.techhades.quranapp.component.application;

import com.techhades.quranapp.QuranImportActivity;
import com.techhades.quranapp.component.activity.PagerActivityComponent;
import com.techhades.quranapp.data.QuranDataProvider;
import com.techhades.quranapp.module.application.ApplicationModule;
import com.techhades.quranapp.module.application.DatabaseModule;
import com.techhades.quranapp.module.application.NetworkModule;
import com.techhades.quranapp.service.QuranDownloadService;
import com.techhades.quranapp.ui.QuranActivity;
import com.techhades.quranapp.ui.TranslationManagerActivity;
import com.techhades.quranapp.ui.fragment.AddTagDialog;
import com.techhades.quranapp.ui.fragment.BookmarksFragment;
import com.techhades.quranapp.ui.fragment.QuranAdvancedSettingsFragment;
import com.techhades.quranapp.ui.fragment.QuranSettingsFragment;
import com.techhades.quranapp.ui.fragment.TagBookmarkDialog;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, DatabaseModule.class, NetworkModule.class})
public interface ApplicationComponent {
  // subcomponents
  PagerActivityComponent.Builder pagerActivityComponentBuilder();

  // content provider
  void inject(QuranDataProvider quranDataProvider);

  // services
  void inject(QuranDownloadService quranDownloadService);

  // activities
  void inject(QuranActivity quranActivity);

  void inject(QuranImportActivity quranImportActivity);

  // fragments
  void inject(BookmarksFragment bookmarksFragment);

  void inject(QuranSettingsFragment fragment);

  void inject(TranslationManagerActivity translationManagerActivity);

  void inject(QuranAdvancedSettingsFragment quranAdvancedSettingsFragment);

  // dialogs
  void inject(TagBookmarkDialog tagBookmarkDialog);

  void inject(AddTagDialog addTagDialog);
}
