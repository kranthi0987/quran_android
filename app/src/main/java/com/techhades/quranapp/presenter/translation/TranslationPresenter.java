package com.techhades.quranapp.presenter.translation;

import android.support.annotation.NonNull;

import com.techhades.quranapp.R;
import com.techhades.quranapp.common.QuranAyahInfo;
import com.techhades.quranapp.data.QuranInfo;
import com.techhades.quranapp.data.SuraAyah;
import com.techhades.quranapp.database.TranslationsDBAdapter;
import com.techhades.quranapp.di.QuranPageScope;
import com.techhades.quranapp.model.translation.TranslationModel;
import com.techhades.quranapp.ui.PagerActivity;
import com.techhades.quranapp.util.QuranSettings;
import com.techhades.quranapp.util.ShareUtil;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

@QuranPageScope
public class TranslationPresenter extends
    BaseTranslationPresenter<TranslationPresenter.TranslationScreen> {
  private final Integer[] pages;
  private final QuranSettings quranSettings;

  @Inject
  TranslationPresenter(TranslationModel translationModel,
                       QuranSettings quranSettings,
                       TranslationsDBAdapter translationsAdapter,
                       Integer... pages) {
    super(translationModel, translationsAdapter);
    this.pages = pages;
    this.quranSettings = quranSettings;
  }

  public void refresh() {
    if (disposable != null) {
      disposable.dispose();
    }

    disposable = Observable.fromArray(pages)
        .flatMap(page -> getVerses(quranSettings.wantArabicInTranslationView(),
            getTranslations(quranSettings), QuranInfo.getVerseRangeForPage(page))
            .toObservable())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<ResultHolder>() {
          @Override
          public void onNext(ResultHolder result) {
            if (translationScreen != null && result.ayahInformation.size() > 0) {
              translationScreen.setVerses(
                  getPage(result.ayahInformation), result.translations, result.ayahInformation);
            }
          }

          @Override
          public void onError(Throwable e) {
          }

          @Override
          public void onComplete() {
          }
        });
  }

  public void onTranslationAction(PagerActivity activity,
                                  QuranAyahInfo ayah,
                                  String[] translationNames,
                                  int actionId) {
    switch (actionId) {
      case R.id.cab_share_ayah_link: {
        SuraAyah bounds = new SuraAyah(ayah.sura, ayah.ayah);
        activity.shareAyahLink(bounds, bounds);
        break;
      }
      case R.id.cab_share_ayah_text:
      case R.id.cab_copy_ayah: {
        String shareText = ShareUtil.getShareText(activity, ayah, translationNames);
        if (actionId == R.id.cab_share_ayah_text) {
          ShareUtil.shareViaIntent(activity, shareText, R.string.share_ayah_text);
        } else {
          ShareUtil.copyToClipboard(activity, shareText);
        }
        break;
      }
    }
  }

  private int getPage(List<QuranAyahInfo> result) {
    final int page;
    if (pages.length == 1) {
      page = pages[0];
    } else {
      QuranAyahInfo ayahInfo = result.get(0);
      page = QuranInfo.getPageFromSuraAyah(ayahInfo.sura, ayahInfo.ayah);
    }
    return page;
  }

  public interface TranslationScreen {
    void setVerses(int page, @NonNull String[] translations, @NonNull List<QuranAyahInfo> verses);
  }
}
