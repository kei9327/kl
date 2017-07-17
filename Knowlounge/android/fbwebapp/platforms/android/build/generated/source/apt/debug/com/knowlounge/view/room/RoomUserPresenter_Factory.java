package com.knowlounge.view.room;

import com.knowlounge.dagger.component.RoomActivityComponent;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomUserPresenter_Factory implements Factory<RoomUserPresenter> {
  private final MembersInjector<RoomUserPresenter> roomUserPresenterMembersInjector;

  private final Provider<RoomActivityComponent> componentProvider;

  public RoomUserPresenter_Factory(
      MembersInjector<RoomUserPresenter> roomUserPresenterMembersInjector,
      Provider<RoomActivityComponent> componentProvider) {
    assert roomUserPresenterMembersInjector != null;
    this.roomUserPresenterMembersInjector = roomUserPresenterMembersInjector;
    assert componentProvider != null;
    this.componentProvider = componentProvider;
  }

  @Override
  public RoomUserPresenter get() {
    return MembersInjectors.injectMembers(
        roomUserPresenterMembersInjector, new RoomUserPresenter(componentProvider.get()));
  }

  public static Factory<RoomUserPresenter> create(
      MembersInjector<RoomUserPresenter> roomUserPresenterMembersInjector,
      Provider<RoomActivityComponent> componentProvider) {
    return new RoomUserPresenter_Factory(roomUserPresenterMembersInjector, componentProvider);
  }
}
