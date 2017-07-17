package com.knowlounge.dagger.modules;

import com.wescan.alo.rtc.RtcChatContext;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class RtcContextModule_ProvideRtcContextFactory implements Factory<RtcChatContext> {
  private final RtcContextModule module;

  public RtcContextModule_ProvideRtcContextFactory(RtcContextModule module) {
    assert module != null;
    this.module = module;
  }

  @Override
  public RtcChatContext get() {
    return Preconditions.checkNotNull(
        module.provideRtcContext(), "Cannot return null from a non-@Nullable @Provides method");
  }

  public static Factory<RtcChatContext> create(RtcContextModule module) {
    return new RtcContextModule_ProvideRtcContextFactory(module);
  }

  /** Proxies {@link RtcContextModule#provideRtcContext()}. */
  public static RtcChatContext proxyProvideRtcContext(RtcContextModule instance) {
    return instance.provideRtcContext();
  }
}
