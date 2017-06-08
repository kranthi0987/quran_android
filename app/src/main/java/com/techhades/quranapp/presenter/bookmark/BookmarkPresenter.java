package com.techhades.quranapp.presenter.bookmark;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.techhades.quranapp.dao.Bookmark;
import com.techhades.quranapp.dao.BookmarkData;
import com.techhades.quranapp.dao.RecentPage;
import com.techhades.quranapp.dao.Tag;
import com.techhades.quranapp.data.Constants;
import com.techhades.quranapp.model.bookmark.BookmarkModel;
import com.techhades.quranapp.model.bookmark.BookmarkResult;
import com.techhades.quranapp.model.translation.ArabicDatabaseUtils;
import com.techhades.quranapp.presenter.Presenter;
import com.techhades.quranapp.ui.fragment.BookmarksFragment;
import com.techhades.quranapp.ui.helpers.QuranRow;
import com.techhades.quranapp.ui.helpers.QuranRowFactory;
import com.techhades.quranapp.util.QuranSettings;
import com.techhades.quranapp.util.QuranUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Singleton
public class BookmarkPresenter implements Presenter<BookmarksFragment> {
  @Snackbar.Duration
  public static final int DELAY_DELETION_DURATION_IN_MS = 4 * 1000; // 4 seconds
  private static final long BOOKMARKS_WITHOUT_TAGS_ID = -1;

  private final Context appContext;
  private final BookmarkModel bookmarkModel;
  private final QuranSettings quranSettings;

  private int sortOrder;
  private boolean groupByTags;
  private boolean showRecents;
  private BookmarkResult cachedData;
  private BookmarksFragment fragment;
  private ArabicDatabaseUtils arabicDatabaseUtils;

  private boolean isRtl;
  private DisposableSingleObserver<BookmarkResult> pendingRemoval;
  private List<QuranRow> itemsToRemove;

  @Inject
  BookmarkPresenter(Context appContext,
                    BookmarkModel bookmarkModel,
                    QuranSettings quranSettings,
                    ArabicDatabaseUtils arabicDatabaseUtils) {
    this.appContext = appContext;
    this.quranSettings = quranSettings;
    this.bookmarkModel = bookmarkModel;
    this.arabicDatabaseUtils = arabicDatabaseUtils;
    sortOrder = quranSettings.getBookmarksSortOrder();
    groupByTags = quranSettings.getBookmarksGroupedByTags();
    showRecents = quranSettings.getShowRecents();
    subscribeToChanges();
  }

  void subscribeToChanges() {
    Observable.merge(bookmarkModel.tagsObservable(),
        bookmarkModel.bookmarksObservable(), bookmarkModel.recentPagesUpdatedObservable())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ignore -> {
          if (fragment != null) {
            requestData(false);
          } else {
            cachedData = null;
          }
        });
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
    quranSettings.setBookmarksSortOrder(this.sortOrder);
    requestData(false);
  }

  public void toggleGroupByTags() {
    groupByTags = !groupByTags;
    quranSettings.setBookmarksGroupedByTags(groupByTags);
    requestData(false);
    Answers.getInstance().logCustom(
        new CustomEvent(groupByTags ? "groupByTags" : "doNotGroupByTags"));
  }

  public void toggleShowRecents() {
    showRecents = !showRecents;
    quranSettings.setShowRecents(showRecents);
    requestData(false);
    Answers.getInstance().logCustom(
        new CustomEvent(showRecents ? "showRecents" : "doNotMinimizeRecents"));
  }

  public boolean isShowingRecents() {
    return showRecents;
  }

  public boolean shouldShowInlineTags() {
    return !groupByTags;
  }

  public boolean isGroupedByTags() {
    return groupByTags;
  }

  public boolean[] getContextualOperationsForItems(List<QuranRow> rows) {
    boolean[] result = new boolean[3];

    int headers = 0;
    int bookmarks = 0;
    for (int i = 0, rowsSize = rows.size(); i < rowsSize; i++) {
      QuranRow row = rows.get(i);
      if (row.isBookmarkHeader()) {
        headers++;
      } else if (row.isBookmark()) {
        bookmarks++;
      }
    }

    result[0] = headers == 1 && bookmarks == 0;
    result[1] = (headers + bookmarks) > 0;
    result[2] = headers == 0 && bookmarks > 0;
    return result;
  }

  public void requestData(boolean canCache) {
    if (canCache && cachedData != null) {
      if (fragment != null) {
        Timber.d("sending cached bookmark data");
        fragment.onNewData(cachedData);
      }
    } else {
      Timber.d("requesting bookmark data from the database");
      getBookmarks(sortOrder, groupByTags);
    }
  }

  public void deleteAfterSomeTime(List<QuranRow> remove) {
    // the fragment just called this, so fragment should be valid
    fragment.onNewData(predictQuranListAfterDeletion(remove));

    if (pendingRemoval != null) {
      // handle a new delete request when one is already happening by adding those items to delete
      // now and un-subscribing from the old request.
      if (itemsToRemove != null) {
        remove.addAll(itemsToRemove);
      }
      cancelDeletion();
    }

    itemsToRemove = remove;
    pendingRemoval = Single.timer(DELAY_DELETION_DURATION_IN_MS, TimeUnit.MILLISECONDS)
        .flatMap(ignore -> removeItemsObservable())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<BookmarkResult>() {

          @Override
          public void onSuccess(BookmarkResult result) {
            pendingRemoval = null;
            cachedData = result;
            if (fragment != null) {
              fragment.onNewData(result);
            }
          }

          @Override
          public void onError(Throwable e) {
          }
        });
  }

  private BookmarkResult predictQuranListAfterDeletion(List<QuranRow> remove) {
    if (cachedData != null) {
      List<QuranRow> placeholder = new ArrayList<>(cachedData.rows.size() - remove.size());
      List<QuranRow> rows = cachedData.rows;
      List<Long> removedTags = new ArrayList<>();
      for (int i = 0, rowsSize = rows.size(); i < rowsSize; i++) {
        QuranRow row = rows.get(i);
        if (!remove.contains(row)) {
          placeholder.add(row);
        }
      }

      for (int i = 0, removedSize = remove.size(); i < removedSize; i++) {
        QuranRow row = remove.get(i);
        if (row.isHeader() && row.tagId > 0) {
          removedTags.add(row.tagId);
        }
      }

      Map<Long, Tag> tagMap;
      if (removedTags.isEmpty()) {
        tagMap = cachedData.tagMap;
      } else {
        tagMap = new HashMap<>(cachedData.tagMap);
        for (int i = 0, removedTagsSize = removedTags.size(); i < removedTagsSize; i++) {
          Long tagId = removedTags.get(i);
          tagMap.remove(tagId);
        }
      }
      return new BookmarkResult(placeholder, tagMap);
    }
    return null;
  }

  private Single<BookmarkResult> removeItemsObservable() {
    return bookmarkModel.removeItemsObservable(new ArrayList<>(itemsToRemove))
        .andThen(getBookmarksListObservable(sortOrder, groupByTags));
  }

  public void cancelDeletion() {
    if (pendingRemoval != null) {
      pendingRemoval.dispose();
      pendingRemoval = null;
      itemsToRemove = null;
    }
  }

  private Single<BookmarkData> getBookmarksWithAyatObservable(int sortOrder) {
    return bookmarkModel.getBookmarkDataObservable(sortOrder)
        .map(bookmarkData -> {
          try {
            return new BookmarkData(bookmarkData.getTags(),
                arabicDatabaseUtils.hydrateAyahText(bookmarkData.getBookmarks()),
                bookmarkData.getRecentPages());
          } catch (Exception e) {
            return bookmarkData;
          }
        });
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  Single<BookmarkResult> getBookmarksListObservable(
      int sortOrder, final boolean groupByTags) {
    return getBookmarksWithAyatObservable(sortOrder)
        .map(bookmarkData -> {
          List<QuranRow> rows = getBookmarkRows(bookmarkData, groupByTags);
          Map<Long, Tag> tagMap = generateTagMap(bookmarkData.getTags());
          return new BookmarkResult(rows, tagMap);
        })
        .subscribeOn(Schedulers.io());
  }

  private void getBookmarks(final int sortOrder, final boolean groupByTags) {
    getBookmarksListObservable(sortOrder, groupByTags)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(result -> {
          // notify the ui if we're attached
          cachedData = result;
          if (fragment != null) {
            if (pendingRemoval != null && itemsToRemove != null) {
              fragment.onNewData(predictQuranListAfterDeletion(itemsToRemove));
            } else {
              fragment.onNewData(result);
            }
          }
        });
  }

  private List<QuranRow> getBookmarkRows(BookmarkData data, boolean groupByTags) {
    List<QuranRow> rows;

    List<Tag> tags = data.getTags();
    List<Bookmark> bookmarks = data.getBookmarks();

    if (groupByTags) {
      rows = getRowsSortedByTags(tags, bookmarks);
    } else {
      rows = getSortedRows(bookmarks);
    }

    List<RecentPage> recentPages = data.getRecentPages();
    int size = recentPages.size();

    if (size > 0) {
      if (!showRecents) {
        // only show the last bookmark if show recents is off
        size = 1;
      }
      rows.add(0, QuranRowFactory.fromRecentPageHeader(appContext, size));
      for (int i = 0; i < size; i++) {
        int page = recentPages.get(i).page;
        if (page < Constants.PAGES_FIRST || page > Constants.PAGES_LAST) {
          page = 1;
        }
        rows.add(i + 1, QuranRowFactory.fromCurrentPage(appContext, page));
      }
    }

    return rows;
  }

  private List<QuranRow> getRowsSortedByTags(List<Tag> tags, List<Bookmark> bookmarks) {
    List<QuranRow> rows = new ArrayList<>();
    // sort by tags, alphabetical
    Map<Long, List<Bookmark>> tagsMapping = generateTagsMapping(tags, bookmarks);
    for (int i = 0, tagsSize = tags.size(); i < tagsSize; i++) {
      Tag tag = tags.get(i);
      rows.add(QuranRowFactory.fromTag(tag));
      List<Bookmark> tagBookmarks = tagsMapping.get(tag.id);
      for (int j = 0, tagBookmarksSize = tagBookmarks.size(); j < tagBookmarksSize; j++) {
        rows.add(QuranRowFactory.fromBookmark(appContext, tagBookmarks.get(j), tag.id));
      }
    }

    // add untagged bookmarks
    List<Bookmark> untagged = tagsMapping.get(BOOKMARKS_WITHOUT_TAGS_ID);
    if (untagged.size() > 0) {
      rows.add(QuranRowFactory.fromNotTaggedHeader(appContext));
      for (int i = 0, untaggedSize = untagged.size(); i < untaggedSize; i++) {
        rows.add(QuranRowFactory.fromBookmark(appContext, untagged.get(i)));
      }
    }
    return rows;
  }

  private List<QuranRow> getSortedRows(List<Bookmark> bookmarks) {
    List<QuranRow> rows = new ArrayList<>(bookmarks.size());
    List<Bookmark> ayahBookmarks = new ArrayList<>();

    // add the page bookmarks directly, save ayah bookmarks for later
    for (int i = 0, bookmarksSize = bookmarks.size(); i < bookmarksSize; i++) {
      Bookmark bookmark = bookmarks.get(i);
      if (bookmark.isPageBookmark()) {
        rows.add(QuranRowFactory.fromBookmark(appContext, bookmark));
      } else {
        ayahBookmarks.add(bookmark);
      }
    }

    // add page bookmarks header if needed
    if (rows.size() > 0) {
      rows.add(0, QuranRowFactory.fromPageBookmarksHeader(appContext));
    }

    // add ayah bookmarks if any
    if (ayahBookmarks.size() > 0) {
      rows.add(QuranRowFactory.fromAyahBookmarksHeader(appContext));
      for (int i = 0, ayahBookmarksSize = ayahBookmarks.size(); i < ayahBookmarksSize; i++) {
        rows.add(QuranRowFactory.fromBookmark(appContext, ayahBookmarks.get(i)));
      }
    }

    return rows;
  }

  private Map<Long, List<Bookmark>> generateTagsMapping(
      List<Tag> tags, List<Bookmark> bookmarks) {
    Set<Long> seenBookmarks = new HashSet<>();
    Map<Long, List<Bookmark>> tagMappings = new HashMap<>();
    for (int i = 0, tagSize = tags.size(); i < tagSize; i++) {
      long id = tags.get(i).id;
      List<Bookmark> matchingBookmarks = new ArrayList<>();
      for (int j = 0, bookmarkSize = bookmarks.size(); j < bookmarkSize; j++) {
        Bookmark bookmark = bookmarks.get(j);
        if (bookmark.tags.contains(id)) {
          matchingBookmarks.add(bookmark);
          seenBookmarks.add(bookmark.id);
        }
      }
      tagMappings.put(id, matchingBookmarks);
    }

    List<Bookmark> untaggedBookmarks = new ArrayList<>();
    for (int i = 0, bookmarksSize = bookmarks.size(); i < bookmarksSize; i++) {
      Bookmark bookmark = bookmarks.get(i);
      if (!seenBookmarks.contains(bookmark.id)) {
        untaggedBookmarks.add(bookmark);
      }
    }
    tagMappings.put(BOOKMARKS_WITHOUT_TAGS_ID, untaggedBookmarks);

    return tagMappings;
  }

  private Map<Long, Tag> generateTagMap(List<Tag> tags) {
    Map<Long, Tag> tagMap = new HashMap<>(tags.size());
    for (int i = 0, size = tags.size(); i < size; i++) {
      Tag tag = tags.get(i);
      tagMap.put(tag.id, tag);
    }
    return tagMap;
  }


  @Override
  public void bind(BookmarksFragment fragment) {
    this.fragment = fragment;
    boolean isRtl = quranSettings.isArabicNames() || QuranUtils.isRtl();
    if (isRtl == this.isRtl) {
      requestData(true);
    } else {
      // don't use the cache if rtl changed
      this.isRtl = isRtl;
      requestData(false);
    }
  }

  @Override
  public void unbind(BookmarksFragment fragment) {
    if (fragment == this.fragment) {
      this.fragment = null;
    }
  }
}
