package com.techhades.quranapp.ui.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.techhades.quranapp.BuildConfig;
import com.techhades.quranapp.R;

public class AboutFragment extends PreferenceFragment {

  private static final String[] sImagePrefKeys =
      new String[]{"madaniImages", "naskhImages", "qaloonImages"};

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.about);

    String flavor = BuildConfig.FLAVOR + "Images";
    PreferenceCategory parent = (PreferenceCategory) findPreference("aboutDataSources");
    for (String string : sImagePrefKeys) {
      if (!string.equals(flavor)) {
        Preference pref = findPreference(string);
        parent.removePreference(pref);
      }
    }
  }
}
