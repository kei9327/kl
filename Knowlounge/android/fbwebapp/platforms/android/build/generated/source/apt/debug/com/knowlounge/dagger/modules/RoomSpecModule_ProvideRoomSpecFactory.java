package com.knowlounge.dagger.modules;

import com.knowlounge.model.RoomSpec;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomSpecModule_ProvideRoomSpecFactory implements Factory<RoomSpec> {
  private final RoomSpecModule module;

  public RoomSpecModule_ProvideRoomSpecFactory(RoomSpecModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public RoomSpec get() {
    return Preconditions.checkNotNull(
        module.provideRoomSpec(), "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<RoomSpec> create(RoomSpecModule module) {
    return new RoomSpecModule_ProvideRoomSpecFactory(module);
  }

  /** Proxies {@link RoomSpecModule#provideRoomSpec()}. */
  public static RoomSpec proxyProvideRoomSpec(RoomSpecModule instance) {
    return instance.provideRoomSpec();
  }
}
