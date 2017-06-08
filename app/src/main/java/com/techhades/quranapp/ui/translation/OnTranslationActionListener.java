package com.techhades.quranapp.ui.translation;

import com.techhades.quranapp.common.QuranAyahInfo;

public interface OnTranslationActionListener {
  void onTranslationAction(QuranAyahInfo ayah, String[] translationNames, int actionId);
}
