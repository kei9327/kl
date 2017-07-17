package com.knowlounge.dagger.modules;

import com.knowlounge.view.room.RoomView;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomMvpViewModule_ProvideMvpViewFactory implements Factory<RoomView> {
  private final RoomMvpViewModule module;

  public RoomMvpViewModule_ProvideMvpViewFactory(RoomMvpViewModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public RoomView get() {
    return Preconditions.checkNotNull(
        module.provideMvpView(), "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<RoomView> create(RoomMvpViewModule module) {
    return new RoomMvpViewModule_ProvideMvpViewFactory(module);
  }

  /** Proxies {@link RoomMvpViewModule#provideMvpView()}. */
  public static RoomView proxyProvideMvpView(RoomMvpViewModule instance) {
    return instance.provideMvpView();
  }
}
