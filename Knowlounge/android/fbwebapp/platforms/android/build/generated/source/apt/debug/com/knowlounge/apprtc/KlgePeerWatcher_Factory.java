package com.knowlounge.apprtc;

import dagger.internal.Factory;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class KlgePeerWatcher_Factory implements Factory<KlgePeerWatcher> {
  private final Provider<String> myUserIdProvider;

  public KlgePeerWatcher_Factory(Provider<String> myUserIdProvider) {
    assert myUserIdProvider != null;
    this.myUserIdProvider = myUserIdProvider;
  }

  @Override
  public KlgePeerWatcher get() {
    return new KlgePeerWatcher(myUserIdProvider.get());
  }

  public static Factory<KlgePeerWatcher> create(Provider<String> myUserIdProvider) {
    return new KlgePeerWatcher_Factory(myUserIdProvider);
  }
}
