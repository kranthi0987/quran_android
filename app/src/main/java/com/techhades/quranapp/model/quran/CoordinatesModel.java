package com.techhades.quranapp.model.quran;

import android.graphics.RectF;
import android.support.v4.util.Pair;

import com.techhades.quranapp.common.AyahBounds;
import com.techhades.quranapp.data.AyahInfoDatabaseHandler;
import com.techhades.quranapp.data.AyahInfoDatabaseProvider;
import com.techhades.quranapp.di.ActivityScope;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

@ActivityScope
public class CoordinatesModel {
  private final AyahInfoDatabaseProvider ayahInfoDatabaseProvider;

  @Inject
  CoordinatesModel(AyahInfoDatabaseProvider ayahInfoDatabaseProvider) {
    this.ayahInfoDatabaseProvider = ayahInfoDatabaseProvider;
  }

  public Observable<Pair<Integer, RectF>> getPageCoordinates(Integer... pages) {
    AyahInfoDatabaseHandler database = ayahInfoDatabaseProvider.getAyahInfoHandler();
    if (database == null) {
      return Observable.error(new NoSuchElementException("No AyahInfoDatabaseHandler found!"));
    }

    return Observable.fromArray(pages)
        .map(page -> new Pair<>(page, database.getPageBounds(page)))
        .subscribeOn(Schedulers.computation());
  }

  public Observable<Pair<Integer, Map<String, List<AyahBounds>>>> getAyahCoordinates(
      Integer... pages) {
    AyahInfoDatabaseHandler database = ayahInfoDatabaseProvider.getAyahInfoHandler();
    if (database == null) {
      return Observable.error(new NoSuchElementException("No AyahInfoDatabaseHandler found!"));
    }

    return Observable.fromArray(pages)
        .map(page -> new Pair<>(page, database.getVersesBoundsForPage(page)))
        .subscribeOn(Schedulers.computation());
  }
}
