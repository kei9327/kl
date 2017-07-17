package com.knowlounge.view.room;

import com.knowlounge.apprtc.KlgePeerWatcher;
import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomActivity_MembersInjector implements MembersInjector<RoomActivity> {
  private final Provider<KlgePeerWatcher> mPeerWatcherProvider;

  private final Provider<RoomUserPresenter> mRoomUserPresenterProvider;

  public RoomActivity_MembersInjector(
      Provider<KlgePeerWatcher> mPeerWatcherProvider,
      Provider<RoomUserPresenter> mRoomUserPresenterProvider) {
    assert mPeerWatcherProvider != null;
    this.mPeerWatcherProvider = mPeerWatcherProvider;
    assert mRoomUserPresenterProvider != null;
    this.mRoomUserPresenterProvider = mRoomUserPresenterProvider;
  }

  public static MembersInjector<RoomActivity> create(
      Provider<KlgePeerWatcher> mPeerWatcherProvider,
      Provider<RoomUserPresenter> mRoomUserPresenterProvider) {
    return new RoomActivity_MembersInjector(mPeerWatcherProvider, mRoomUserPresenterProvider);
  }

  @Override
  public void injectMembers(RoomActivity instance) {
    if (instance == null) {
      throw new NullPointerException("Cannot inject members into a null reference");
    }
    instance.mPeerWatcher = mPeerWatcherProvider.get();
    instance.mRoomUserPresenter = mRoomUserPresenterProvider.get();
  }

  public static void injectMPeerWatcher(
      RoomActivity instance, Provider<KlgePeerWatcher> mPeerWatcherProvider) {
    instance.mPeerWatcher = mPeerWatcherProvider.get();
  }

  public static void injectMRoomUserPresenter(
      RoomActivity instance, Provider<RoomUserPresenter> mRoomUserPresenterProvider) {
    instance.mRoomUserPresenter = mRoomUserPresenterProvider.get();
  }
}
