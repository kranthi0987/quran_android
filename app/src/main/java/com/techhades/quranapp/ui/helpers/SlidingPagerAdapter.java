package com.techhades.quranapp.ui.helpers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.techhades.quranapp.R;
import com.techhades.quranapp.ui.fragment.AyahPlaybackFragment;
import com.techhades.quranapp.ui.fragment.AyahTranslationFragment;
import com.techhades.quranapp.ui.fragment.TagBookmarkDialog;
import com.techhades.quranapp.widgets.IconPageIndicator;

public class SlidingPagerAdapter extends FragmentStatePagerAdapter implements
    IconPageIndicator.IconPagerAdapter {

  public static final int TAG_PAGE = 0;
  public static final int TRANSLATION_PAGE = 1;
  public static final int AUDIO_PAGE = 2;
  public static final int[] PAGES = {
      TAG_PAGE, TRANSLATION_PAGE, AUDIO_PAGE
  };
  public static final int[] PAGE_ICONS = {
      R.drawable.ic_tag, R.drawable.ic_translation, R.drawable.ic_play
  };

  private boolean mIsRtl;

  public SlidingPagerAdapter(FragmentManager fm, boolean isRtl) {
    super(fm, "sliding");
    mIsRtl = isRtl;
  }

  @Override
  public int getCount() {
    return PAGES.length;
  }

  public int getPagePosition(int page) {
    return mIsRtl ? (PAGES.length - 1) - page : page;
  }

  @Override
  public Fragment getItem(int position) {
    final int pos = getPagePosition(position);
    switch (pos) {
      case TAG_PAGE:
        return new TagBookmarkDialog();
      case TRANSLATION_PAGE:
        return new AyahTranslationFragment();
      case AUDIO_PAGE:
        return new AyahPlaybackFragment();
    }
    return null;
  }

  @Override
  public int getIconResId(int index) {
    return PAGE_ICONS[getPagePosition(index)];
  }

}
