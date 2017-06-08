package com.techhades.quranapp.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.techhades.quranapp.R;
import com.techhades.quranapp.common.LocalTranslation;
import com.techhades.quranapp.common.QuranAyahInfo;
import com.techhades.quranapp.data.VerseRange;
import com.techhades.quranapp.presenter.translation.InlineTranslationPresenter;
import com.techhades.quranapp.ui.PagerActivity;
import com.techhades.quranapp.ui.util.TranslationsSpinnerAdapter;
import com.techhades.quranapp.util.QuranSettings;
import com.techhades.quranapp.widgets.InlineTranslationView;
import com.techhades.quranapp.widgets.QuranSpinner;

import java.util.List;

import javax.inject.Inject;

public class AyahTranslationFragment extends AyahActionFragment
    implements InlineTranslationPresenter.TranslationScreen {

  @Inject
  QuranSettings quranSettings;
  @Inject
  InlineTranslationPresenter translationPresenter;
  private ProgressBar progressBar;
  private InlineTranslationView translationView;
  private View emptyState;
  private View translationControls;
  private QuranSpinner translator;
  private TranslationsSpinnerAdapter translationAdapter;
  private List<LocalTranslation> translations;
  private View.OnClickListener onClickListener = v -> {
    final Activity activity = getActivity();
    if (activity instanceof PagerActivity) {
      final PagerActivity pagerActivity = (PagerActivity) activity;

      switch (v.getId()) {
        case R.id.get_translations_button:
          pagerActivity.startTranslationManager();
          break;
        case R.id.next_ayah:
          pagerActivity.nextAyah();
          break;
        case R.id.previous_ayah:
          pagerActivity.previousAyah();
          break;
      }
    }
  };

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    ((PagerActivity) getActivity()).getPagerActivityComponent().inject(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(
        R.layout.translation_panel, container, false);

    translator = (QuranSpinner) view.findViewById(R.id.translator);
    translationView = (InlineTranslationView) view.findViewById(R.id.translation_view);
    progressBar = (ProgressBar) view.findViewById(R.id.progress);
    emptyState = view.findViewById(R.id.empty_state);
    translationControls = view.findViewById(R.id.controls);
    final View next = translationControls.findViewById(R.id.next_ayah);
    next.setOnClickListener(onClickListener);

    final View prev = translationControls.findViewById(R.id.previous_ayah);
    prev.setOnClickListener(onClickListener);

    final Button getTranslations =
        (Button) view.findViewById(R.id.get_translations_button);
    getTranslations.setOnClickListener(onClickListener);
    return view;
  }

  @Override
  public void onResume() {
    // currently needs to be before we call super.onResume
    translationPresenter.bind(this);
    super.onResume();
  }

  @Override
  public void onPause() {
    translationPresenter.unbind(this);
    super.onPause();
  }

  public void refreshView() {
    if (start == null || end == null) {
      return;
    }

    final Activity activity = getActivity();
    if (activity instanceof PagerActivity) {
      PagerActivity pagerActivity = (PagerActivity) activity;
      if (translations == null) {
        translations = pagerActivity.getTranslations();
      }

      if (translations == null || translations.size() == 0) {
        progressBar.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        translationControls.setVisibility(View.GONE);
        return;
      }

      if (translationAdapter == null) {
        translationAdapter = new TranslationsSpinnerAdapter(activity,
            R.layout.translation_ab_spinner_item,
            pagerActivity.getTranslationNames(),
            translations,
            quranSettings.getActiveTranslations(),
            selectedItems -> {
              quranSettings.setActiveTranslations(selectedItems);
              refreshView();
            });
        translator.setAdapter(translationAdapter);
      }

      if (start.equals(end)) {
        translationControls.setVisibility(View.VISIBLE);
      } else {
        translationControls.setVisibility(View.GONE);
      }

      VerseRange verseRange = new VerseRange(start.sura, start.ayah, end.sura, end.ayah);
      translationPresenter.refresh(verseRange);
    }
  }

  @Override
  public void setVerses(@NonNull String[] translations, @NonNull List<QuranAyahInfo> verses) {
    progressBar.setVisibility(View.GONE);
    if (verses.size() > 0) {
      emptyState.setVisibility(View.GONE);
      translationView.setAyahs(translations, verses);
    } else {
      emptyState.setVisibility(View.VISIBLE);
    }
  }
}
