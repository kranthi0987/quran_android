package com.techhades.quranapp.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.techhades.quranapp.common.AyahBounds;
import com.techhades.quranapp.dao.Bookmark;
import com.techhades.quranapp.module.fragment.QuranPageModule;
import com.techhades.quranapp.presenter.quran.QuranPagePresenter;
import com.techhades.quranapp.presenter.quran.QuranPageScreen;
import com.techhades.quranapp.presenter.quran.ayahtracker.AyahImageTrackerItem;
import com.techhades.quranapp.presenter.quran.ayahtracker.AyahScrollableImageTrackerItem;
import com.techhades.quranapp.presenter.quran.ayahtracker.AyahTrackerItem;
import com.techhades.quranapp.presenter.quran.ayahtracker.AyahTrackerPresenter;
import com.techhades.quranapp.ui.PagerActivity;
import com.techhades.quranapp.ui.helpers.AyahSelectedListener;
import com.techhades.quranapp.ui.helpers.AyahTracker;
import com.techhades.quranapp.ui.helpers.HighlightType;
import com.techhades.quranapp.ui.helpers.QuranPage;
import com.techhades.quranapp.ui.util.PageController;
import com.techhades.quranapp.util.QuranSettings;
import com.techhades.quranapp.widgets.HighlightingImageView;
import com.techhades.quranapp.widgets.QuranImagePageLayout;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

import static com.techhades.quranapp.ui.helpers.AyahSelectedListener.EventType;

public class QuranPageFragment extends Fragment implements PageController,
    QuranPage, QuranPageScreen, AyahTrackerPresenter.AyahInteractionHandler {
  private static final String PAGE_NUMBER_EXTRA = "pageNumber";
  @Inject
  QuranSettings quranSettings;
  @Inject
  QuranPagePresenter quranPagePresenter;
  @Inject
  AyahTrackerPresenter ayahTrackerPresenter;
  @Inject
  AyahSelectedListener ayahSelectedListener;
  private int pageNumber;
  private AyahTrackerItem[] ayahTrackerItems;
  private HighlightingImageView imageView;
  private QuranImagePageLayout quranPageLayout;
  private boolean ayahCoordinatesError;

  public static QuranPageFragment newInstance(int page) {
    final QuranPageFragment f = new QuranPageFragment();
    final Bundle args = new Bundle();
    args.putInt(PAGE_NUMBER_EXTRA, page);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    final Context context = getActivity();
    quranPageLayout = new QuranImagePageLayout(context);
    quranPageLayout.setPageController(this, pageNumber);
    imageView = quranPageLayout.getImageView();
    return quranPageLayout;
  }

  @Override
  public void updateView() {
    if (isAdded()) {
      quranPageLayout.updateView(quranSettings);
      if (!quranSettings.highlightBookmarks()) {
        imageView.unHighlight(HighlightType.BOOKMARK);
      }
      quranPagePresenter.refresh();
    }
  }

  @Override
  public AyahTracker getAyahTracker() {
    return ayahTrackerPresenter;
  }

  @Override
  public AyahTrackerItem[] getAyahTrackerItems() {
    if (ayahTrackerItems == null) {
      ayahTrackerItems = new AyahTrackerItem[]{
          quranPageLayout.canScroll() ?
              new AyahScrollableImageTrackerItem(pageNumber, quranPageLayout, imageView) :
              new AyahImageTrackerItem(pageNumber, imageView)
      };
    }
    return ayahTrackerItems;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    pageNumber = getArguments().getInt(PAGE_NUMBER_EXTRA);
    ((PagerActivity) getActivity()).getPagerActivityComponent()
        .quranPageComponentBuilder()
        .withQuranPageModule(new QuranPageModule(pageNumber))
        .build()
        .inject(this);
  }

  @Override
  public void onDetach() {
    ayahSelectedListener = null;
    super.onDetach();
  }

  @Override
  public void onStart() {
    super.onStart();
    quranPagePresenter.bind(this);
    ayahTrackerPresenter.bind(this);
  }

  @Override
  public void onStop() {
    quranPagePresenter.unbind(this);
    ayahTrackerPresenter.unbind(this);
    super.onStop();
  }

  public void cleanup() {
    Timber.d("cleaning up page %d", pageNumber);
    if (quranPageLayout != null) {
      imageView.setImageDrawable(null);
      quranPageLayout = null;
    }
  }

  @Override
  public void setPageCoordinates(int page, RectF pageCoordinates) {
    ayahTrackerPresenter.setPageBounds(page, pageCoordinates);
  }

  @Override
  public void setBookmarksOnPage(List<Bookmark> bookmarks) {
    ayahTrackerPresenter.setAyahBookmarks(bookmarks);
  }

  @Override
  public void setAyahCoordinatesData(int page, Map<String, List<AyahBounds>> coordinates) {
    ayahTrackerPresenter.setAyahCoordinates(page, coordinates);
    ayahCoordinatesError = false;
  }

  @Override
  public void setAyahCoordinatesError() {
    ayahCoordinatesError = true;
  }

  @Override
  public void onScrollChanged(int x, int y, int oldx, int oldy) {
    PagerActivity activity = (PagerActivity) getActivity();
    if (activity != null) {
      activity.onQuranPageScroll(y);
    }
  }

  @Override
  public void setPageDownloadError(@StringRes int errorMessage) {
    quranPageLayout.showError(errorMessage);
    quranPageLayout.setOnClickListener(v -> ayahSelectedListener.onClick(EventType.SINGLE_TAP));
  }

  @Override
  public void setPageBitmap(int page, @NonNull Bitmap pageBitmap) {
    imageView.setImageDrawable(new BitmapDrawable(getResources(), pageBitmap));
  }

  @Override
  public void hidePageDownloadError() {
    quranPageLayout.hideError();
    quranPageLayout.setOnClickListener(null);
    quranPageLayout.setClickable(false);
  }

  @Override
  public void handleRetryClicked() {
    hidePageDownloadError();
    quranPagePresenter.downloadImages();
  }

  @Override
  public boolean handleTouchEvent(MotionEvent event, EventType eventType, int page) {
    return isVisible() && ayahTrackerPresenter.handleTouchEvent(getActivity(), event, eventType,
        page, ayahSelectedListener, ayahCoordinatesError);
  }
}
