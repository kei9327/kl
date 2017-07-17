package com.knowlounge.dagger.modules;

import com.knowlounge.view.room.Navigator;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class AppModule_ProvideNavigatorFactory implements Factory<Navigator> {
  private final AppModule module;

  public AppModule_ProvideNavigatorFactory(AppModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public Navigator get() {
    return Preconditions.checkNotNull(
        module.provideNavigator(), "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<Navigator> create(AppModule module) {
    return new AppModule_ProvideNavigatorFactory(module);
  }

  /** Proxies {@link AppModule#provideNavigator()}. */
  public static Navigator proxyProvideNavigator(AppModule instance) {
    return instance.provideNavigator();
  }
}
