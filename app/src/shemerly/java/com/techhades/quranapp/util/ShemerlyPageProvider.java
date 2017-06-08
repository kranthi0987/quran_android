package com.techhades.quranapp.util;

public class ShemerlyPageProvider implements QuranScreenInfo.PageProvider {

  @Override
  public String getWidthParameter() {
    return "1200";
  }

  @Override
  public String getTabletWidthParameter() {
    // use the same size for tablet landscape
    return getWidthParameter();
  }

  @Override
  public void setOverrideParameter(String parameter) {
    // override parameter is irrelevant for shemerly pages
  }
}
