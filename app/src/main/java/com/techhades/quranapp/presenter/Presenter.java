package com.techhades.quranapp.presenter;

public interface Presenter<T> {
  void bind(T what);

  void unbind(T what);
}
