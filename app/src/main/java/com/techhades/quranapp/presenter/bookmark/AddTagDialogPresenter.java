package com.techhades.quranapp.presenter.bookmark;

import com.techhades.quranapp.dao.Tag;
import com.techhades.quranapp.model.bookmark.BookmarkModel;
import com.techhades.quranapp.presenter.Presenter;
import com.techhades.quranapp.ui.fragment.AddTagDialog;

import javax.inject.Inject;

public class AddTagDialogPresenter implements Presenter<AddTagDialog> {
  private BookmarkModel mBookmarkModel;

  @Inject
  AddTagDialogPresenter(BookmarkModel bookmarkModel) {
    mBookmarkModel = bookmarkModel;
  }

  public void addTag(String tagName) {
    mBookmarkModel.addTagObservable(tagName)
        .subscribe();
  }

  public void updateTag(Tag tag) {
    mBookmarkModel.updateTag(tag)
        .subscribe();
  }

  @Override
  public void bind(AddTagDialog dialog) {
  }

  @Override
  public void unbind(AddTagDialog dialog) {
  }
}
