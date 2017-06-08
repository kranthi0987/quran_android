package com.techhades.quranapp.ui.helpers;

import android.graphics.drawable.BitmapDrawable;

import com.techhades.quranapp.common.Response;

public interface PageDownloadListener {
  void onLoadImageResponse(BitmapDrawable drawable, Response response);
}
