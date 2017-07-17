package com.knowlounge.view.room;

import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomPresenter_Factory implements Factory<RoomPresenter> {
  private final MembersInjector<RoomPresenter> roomPresenterMembersInjector;

  private final Provider<RoomView> viewProvider;

  public RoomPresenter_Factory(
      MembersInjector<RoomPresenter> roomPresenterMembersInjector,
      Provider<RoomView> viewProvider) {
    assert roomPresenterMembersInjector != null;
    this.roomPresenterMembersInjector = roomPresenterMembersInjector;
    assert viewProvider != null;
    this.viewProvider = viewProvider;
  }

  @Override
  public RoomPresenter get() {
    return MembersInjectors.injectMembers(
        roomPresenterMembersInjector, new RoomPresenter(viewProvider.get()));
  }

  public static Factory<RoomPresenter> create(
      MembersInjector<RoomPresenter> roomPresenterMembersInjector,
      Provider<RoomView> viewProvider) {
    return new RoomPresenter_Factory(roomPresenterMembersInjector, viewProvider);
  }
}
