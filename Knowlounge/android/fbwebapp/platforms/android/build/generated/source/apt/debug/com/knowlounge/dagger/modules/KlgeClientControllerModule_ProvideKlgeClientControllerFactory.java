package com.knowlounge.dagger.modules;

import com.knowlounge.apprtc.KlgeClientController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class KlgeClientControllerModule_ProvideKlgeClientControllerFactory
    implements Factory<KlgeClientController> {
  private final KlgeClientControllerModule module;

  public KlgeClientControllerModule_ProvideKlgeClientControllerFactory(
      KlgeClientControllerModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public KlgeClientController get() {
    return Preconditions.checkNotNull(
        module.provideKlgeClientController(),
        "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<KlgeClientController> create(KlgeClientControllerModule module) {
    return new KlgeClientControllerModule_ProvideKlgeClientControllerFactory(module);
  }

  /** Proxies {@link KlgeClientControllerModule#provideKlgeClientController()}. */
  public static KlgeClientController proxyProvideKlgeClientController(
      KlgeClientControllerModule instance) {
    return instance.provideKlgeClientController();
  }
}
