package com.techhades.quranapp.model.translation;

import android.content.Context;

import com.techhades.quranapp.common.QuranText;
import com.techhades.quranapp.data.QuranDataProvider;
import com.techhades.quranapp.data.VerseRange;
import com.techhades.quranapp.database.DatabaseHandler;
import com.techhades.quranapp.di.ActivityScope;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;

@ActivityScope
public class TranslationModel {
  private Context appContext;

  @Inject
  TranslationModel(Context appContext) {
    this.appContext = appContext;
  }

  public Single<List<QuranText>> getArabicFromDatabase(VerseRange verses) {
    return getVersesFromDatabase(verses,
        QuranDataProvider.QURAN_ARABIC_DATABASE, DatabaseHandler.TextType.ARABIC);
  }

  public Single<List<QuranText>> getTranslationFromDatabase(VerseRange verses, String db) {
    return getVersesFromDatabase(verses, db, DatabaseHandler.TextType.TRANSLATION);
  }

  private Single<List<QuranText>> getVersesFromDatabase(VerseRange verses,
                                                        String database,
                                                        @DatabaseHandler.TextType int type) {
    return Single.fromCallable(() -> {
      DatabaseHandler databaseHandler = DatabaseHandler.getDatabaseHandler(appContext, database);
      return databaseHandler.getVerses(verses, type);
    });
  }
}
