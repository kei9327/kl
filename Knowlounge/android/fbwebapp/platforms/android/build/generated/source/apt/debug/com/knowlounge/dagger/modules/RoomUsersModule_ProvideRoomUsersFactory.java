package com.knowlounge.dagger.modules;

import com.knowlounge.model.RoomUsers;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomUsersModule_ProvideRoomUsersFactory implements Factory<RoomUsers> {
  private final RoomUsersModule module;

  public RoomUsersModule_ProvideRoomUsersFactory(RoomUsersModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public RoomUsers get() {
    return Preconditions.checkNotNull(
        module.provideRoomUsers(), "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<RoomUsers> create(RoomUsersModule module) {
    return new RoomUsersModule_ProvideRoomUsersFactory(module);
  }

  /** Proxies {@link RoomUsersModule#provideRoomUsers()}. */
  public static RoomUsers proxyProvideRoomUsers(RoomUsersModule instance) {
    return instance.provideRoomUsers();
  }
}
