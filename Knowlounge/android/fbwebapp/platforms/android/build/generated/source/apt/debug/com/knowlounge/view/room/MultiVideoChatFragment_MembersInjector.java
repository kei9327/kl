package com.knowlounge.view.room;

import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class MultiVideoChatFragment_MembersInjector
    implements MembersInjector<MultiVideoChatFragment> {
  private final Provider<RoomPresenter> mPresenterProvider;

  public MultiVideoChatFragment_MembersInjector(Provider<RoomPresenter> mPresenterProvider) {
    assert mPresenterProvider != null;
    this.mPresenterProvider = mPresenterProvider;
  }

  public static MembersInjector<MultiVideoChatFragment> create(
      Provider<RoomPresenter> mPresenterProvider) {
    return new MultiVideoChatFragment_MembersInjector(mPresenterProvider);
  }

  @Override
  public void injectMembers(MultiVideoChatFragment instance) {
    if (instance == null) {
      throw new NullPointerException("Cannot inject members into a null reference");
    }
    instance.mPresenter = mPresenterProvider.get();
  }

  public static void injectMPresenter(
      MultiVideoChatFragment instance, Provider<RoomPresenter> mPresenterProvider) {
    instance.mPresenter = mPresenterProvider.get();
  }
}
