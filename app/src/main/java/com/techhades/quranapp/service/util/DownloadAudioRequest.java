package com.techhades.quranapp.service.util;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.techhades.quranapp.common.QariItem;
import com.techhades.quranapp.data.SuraAyah;
import com.techhades.quranapp.util.AudioUtils;

public class DownloadAudioRequest extends AudioRequest {

  public static final Creator<DownloadAudioRequest> CREATOR = new Creator<DownloadAudioRequest>() {
    public DownloadAudioRequest createFromParcel(Parcel source) {
      return new DownloadAudioRequest(source);
    }

    public DownloadAudioRequest[] newArray(int size) {
      return new DownloadAudioRequest[size];
    }
  };
  @NonNull
  private final QariItem qariItem;
  private String localDirectoryPath = null;

  public DownloadAudioRequest(String baseUrl, SuraAyah verse,
                              @NonNull QariItem qariItem, String localPath) {
    super(baseUrl, verse);
    this.qariItem = qariItem;
    localDirectoryPath = localPath;
  }

  private DownloadAudioRequest(Parcel in) {
    super(in);
    this.qariItem = in.readParcelable(QariItem.class.getClassLoader());
    this.localDirectoryPath = in.readString();
  }

  @NonNull
  public QariItem getQariItem() {
    return qariItem;
  }

  public String getLocalPath() {
    return localDirectoryPath;
  }

  @Override
  public boolean haveSuraAyah(int sura, int ayah) {
    return AudioUtils.haveSuraAyahForQari(localDirectoryPath, sura, ayah);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeParcelable(this.qariItem, 0);
    dest.writeString(this.localDirectoryPath);
  }
}
