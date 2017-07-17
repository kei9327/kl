package com.knowlounge.dagger.modules;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RoomSpecModule_ProvideUserNoFactory implements Factory<String> {
  private final RoomSpecModule module;

  public RoomSpecModule_ProvideUserNoFactory(RoomSpecModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public String get() {
    return Preconditions.checkNotNull(
        module.provideUserNo(), "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<String> create(RoomSpecModule module) {
    return new RoomSpecModule_ProvideUserNoFactory(module);
  }

  /** Proxies {@link RoomSpecModule#provideUserNo()}. */
  public static String proxyProvideUserNo(RoomSpecModule instance) {
    return instance.provideUserNo();
  }
}
