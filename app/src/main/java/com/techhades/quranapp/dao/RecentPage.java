package com.techhades.quranapp.dao;

public class RecentPage {
  public final int page;
  public final long timestamp;

  public RecentPage(int page, long timestamp) {
    this.page = page;
    this.timestamp = timestamp;
  }
}
