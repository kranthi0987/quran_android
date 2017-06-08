package com.techhades.quranapp.presenter.translation;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;

import com.crashlytics.android.Crashlytics;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.techhades.quranapp.common.LocalTranslation;
import com.techhades.quranapp.dao.translation.Translation;
import com.techhades.quranapp.dao.translation.TranslationItem;
import com.techhades.quranapp.dao.translation.TranslationList;
import com.techhades.quranapp.data.Constants;
import com.techhades.quranapp.database.DatabaseHandler;
import com.techhades.quranapp.database.TranslationsDBAdapter;
import com.techhades.quranapp.presenter.Presenter;
import com.techhades.quranapp.ui.TranslationManagerActivity;
import com.techhades.quranapp.util.QuranFileUtils;
import com.techhades.quranapp.util.QuranSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import timber.log.Timber;

@Singleton
public class TranslationManagerPresenter implements Presenter<TranslationManagerActivity> {
  private static final String WEB_SERVICE_ENDPOINT = "data/translations.php?v=4";
  private static final String CACHED_RESPONSE_FILE_NAME = "translations.v4.cache";

  private final Context appContext;
  private final OkHttpClient okHttpClient;
  private final QuranSettings quranSettings;
  private final TranslationsDBAdapter translationsDBAdapter;

  @VisibleForTesting
  String host;
  private TranslationManagerActivity currentActivity;

  @Inject
  TranslationManagerPresenter(Context appContext,
                              OkHttpClient okHttpClient,
                              QuranSettings quranSettings,
                              TranslationsDBAdapter dbAdapter) {
    this.host = Constants.HOST;
    this.appContext = appContext;
    this.okHttpClient = okHttpClient;
    this.quranSettings = quranSettings;
    this.translationsDBAdapter = dbAdapter;
  }

  public void checkForUpdates() {
    getTranslationsList(true);
  }

  public void getTranslationsList(boolean forceDownload) {
    Observable.concat(
        getCachedTranslationListObservable(forceDownload), getRemoteTranslationListObservable())
        .filter(translationList -> translationList.translations != null)
        .firstElement()
        .filter(translationList -> !translationList.translations.isEmpty())
        .map(translationList -> mergeWithServerTranslations(translationList.translations))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DisposableMaybeObserver<List<TranslationItem>>() {
          @Override
          public void onSuccess(List<TranslationItem> translationItems) {
            if (currentActivity != null) {
              currentActivity.onTranslationsUpdated(translationItems);
            }

            // used for marking upgrades, irrespective of whether or not there is a bound activity
            boolean updatedTranslations = false;
            for (TranslationItem item : translationItems) {
              if (item.needsUpgrade()) {
                updatedTranslations = true;
                break;
              }
            }
            quranSettings.setHaveUpdatedTranslations(updatedTranslations);
          }

          @Override
          public void onError(Throwable e) {
            if (currentActivity != null) {
              currentActivity.onErrorDownloadTranslations();
            }
          }

          @Override
          public void onComplete() {
            if (currentActivity != null) {
              currentActivity.onErrorDownloadTranslations();
            }
          }
        });
  }

  public void updateItem(final TranslationItem item) {
    Observable.fromCallable(() ->
        translationsDBAdapter.writeTranslationUpdates(Collections.singletonList(item))
    ).subscribeOn(Schedulers.io())
        .subscribe();
  }

  Observable<TranslationList> getCachedTranslationListObservable(final boolean forceDownload) {
    return Observable.defer(() -> {
      boolean isCacheStale = System.currentTimeMillis() -
          quranSettings.getLastUpdatedTranslationDate() > Constants.MIN_TRANSLATION_REFRESH_TIME;
      if (forceDownload || isCacheStale) {
        return Observable.empty();
      }

      try {
        File cachedFile = getCachedFile();
        if (cachedFile.exists()) {
          Moshi moshi = new Moshi.Builder().build();
          JsonAdapter<TranslationList> jsonAdapter = moshi.adapter(TranslationList.class);
          return Observable.just(jsonAdapter.fromJson(Okio.buffer(Okio.source(cachedFile))));
        }
      } catch (Exception e) {
        Crashlytics.logException(e);
      }
      return Observable.empty();
    });
  }

  Observable<TranslationList> getRemoteTranslationListObservable() {
    return Observable.fromCallable(() -> {
      Request request = new Request.Builder()
          .url(host + WEB_SERVICE_ENDPOINT)
          .build();
      Response response = okHttpClient.newCall(request).execute();

      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<TranslationList> jsonAdapter = moshi.adapter(TranslationList.class);

      ResponseBody responseBody = response.body();
      TranslationList result = jsonAdapter.fromJson(responseBody.source());
      responseBody.close();
      return result;
    }).doOnNext(translationList -> {
      if (translationList.translations != null && !translationList.translations.isEmpty()) {
        writeTranslationList(translationList);
      }
    });
  }

  void writeTranslationList(TranslationList list) {
    File cacheFile = getCachedFile();
    try {
      File directory = cacheFile.getParentFile();
      boolean directoryExists = directory.mkdirs() || directory.isDirectory();
      if (directoryExists) {
        if (cacheFile.exists()) {
          cacheFile.delete();
        }
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<TranslationList> jsonAdapter = moshi.adapter(TranslationList.class);
        BufferedSink sink = Okio.buffer(Okio.sink(cacheFile));
        jsonAdapter.toJson(sink, list);
        sink.close();
        quranSettings.setLastUpdatedTranslationDate(System.currentTimeMillis());
      }
    } catch (Exception e) {
      cacheFile.delete();
      Crashlytics.logException(e);
    }
  }

  private File getCachedFile() {
    String dir = QuranFileUtils.getQuranDatabaseDirectory(appContext);
    return new File(dir + File.separator + CACHED_RESPONSE_FILE_NAME);
  }

  private List<TranslationItem> mergeWithServerTranslations(List<Translation> serverTranslations) {
    List<TranslationItem> results = new ArrayList<>(serverTranslations.size());
    SparseArray<LocalTranslation> localTranslations = translationsDBAdapter.getTranslationsHash();
    String databaseDir = QuranFileUtils.getQuranDatabaseDirectory(appContext);

    List<TranslationItem> updates = new ArrayList<>();
    for (int i = 0, count = serverTranslations.size(); i < count; i++) {
      Translation translation = serverTranslations.get(i);
      LocalTranslation local = localTranslations.get(translation.id);

      File dbFile = new File(databaseDir, translation.fileName);
      boolean exists = dbFile.exists();

      TranslationItem item;
      if (exists) {
        int version = local == null ? getVersionFromDatabase(translation.fileName) : local.version;
        item = new TranslationItem(translation, version);
      } else {
        item = new TranslationItem(translation);
      }

      if (exists && !item.exists()) {
        // delete the file, it has been corrupted
        if (dbFile.delete()) {
          exists = false;
        }
      }

      if ((local == null && exists) || (local != null && !exists)) {
        updates.add(item);
      } else if (local != null && local.languageCode == null) {
        // older items don't have a language code
        updates.add(item);
      }
      results.add(item);
    }

    if (!updates.isEmpty()) {
      translationsDBAdapter.writeTranslationUpdates(updates);
    }
    return results;
  }

  private int getVersionFromDatabase(String filename) {
    try {
      DatabaseHandler handler = DatabaseHandler.getDatabaseHandler(appContext, filename);
      if (handler.validDatabase()) {
        return handler.getTextVersion();
      }
    } catch (Exception e) {
      Timber.d(e, "exception opening database: %s", filename);
    }
    return 0;
  }


  @Override
  public void bind(TranslationManagerActivity activity) {
    currentActivity = activity;
  }

  @Override
  public void unbind(TranslationManagerActivity activity) {
    if (activity == currentActivity) {
      currentActivity = null;
    }
  }
}
