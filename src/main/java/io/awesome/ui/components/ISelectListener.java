package io.awesome.ui.components;

import io.awesome.ui.views.SelectCallback;

public interface ISelectListener<L> {
  void addSelectListener(SelectCallback<L> event);
}
