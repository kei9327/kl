package com.knowlounge.dagger.component;

import com.knowlounge.apprtc.KlgeClientController;
import com.knowlounge.dagger.modules.KlgeClientControllerModule;
import com.knowlounge.dagger.modules.KlgeClientControllerModule_ProvideKlgeClientControllerFactory;
import com.knowlounge.dagger.modules.RoomMvpViewModule;
import com.knowlounge.dagger.modules.RoomMvpViewModule_ProvideMvpViewFactory;
import com.knowlounge.dagger.modules.RtcContextModule;
import com.knowlounge.dagger.modules.RtcContextModule_ProvideRtcContextFactory;
import com.knowlounge.view.room.MultiVideoChatFragment;
import com.knowlounge.view.room.MultiVideoChatFragment_MembersInjector;
import com.knowlounge.view.room.RoomPresenter;
import com.knowlounge.view.room.RoomPresenter_Factory;
import com.knowlounge.view.room.RoomView;
import com.wescan.alo.rtc.RtcChatContext;
import dagger.MembersInjector;
import dagger.internal.DoubleCheck;
import dagger.internal.MembersInjectors;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerMultiVideoChatFragmentComponent
    implements MultiVideoChatFragmentComponent {
  private Provider<RoomView> provideMvpViewProvider;

  private Provider<RoomPresenter> roomPresenterProvider;

  private MembersInjector<MultiVideoChatFragment> multiVideoChatFragmentMembersInjector;

  private Provider<RtcChatContext> provideRtcContextProvider;

  private Provider<KlgeClientController> provideKlgeClientControllerProvider;

  private DaggerMultiVideoChatFragmentComponent(Builder builder) {
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {

    this.provideMvpViewProvider =
        RoomMvpViewModule_ProvideMvpViewFactory.create(builder.roomMvpViewModule);

    this.roomPresenterProvider =
        DoubleCheck.provider(
            RoomPresenter_Factory.create(
                MembersInjectors.<RoomPresenter>noOp(), provideMvpViewProvider));

    this.multiVideoChatFragmentMembersInjector =
        MultiVideoChatFragment_MembersInjector.create(roomPresenterProvider);

    this.provideRtcContextProvider =
        RtcContextModule_ProvideRtcContextFactory.create(builder.rtcContextModule);

    this.provideKlgeClientControllerProvider =
        DoubleCheck.provider(
            KlgeClientControllerModule_ProvideKlgeClientControllerFactory.create(
                builder.klgeClientControllerModule));
  }

  @Override
  public void inject(MultiVideoChatFragment fragment) {
    multiVideoChatFragmentMembersInjector.injectMembers(fragment);
  }

  @Override
  public RtcChatContext getRtcContext() {
    return provideRtcContextProvider.get();
  }

  @Override
  public RoomView getRoomView() {
    return provideMvpViewProvider.get();
  }

  @Override
  public KlgeClientController getController() {
    return provideKlgeClientControllerProvider.get();
  }

  public static final class Builder {
    private RoomMvpViewModule roomMvpViewModule;

    private RtcContextModule rtcContextModule;

    private KlgeClientControllerModule klgeClientControllerModule;

    private AppComponent appComponent;

    private Builder() {}

    public MultiVideoChatFragmentComponent build() {
      if (roomMvpViewModule == null) {
        throw new IllegalStateException(
            RoomMvpViewModule.class.getCanonicalName() + " must be set");
      }
      if (rtcContextModule == null) {
        throw new IllegalStateException(RtcContextModule.class.getCanonicalName() + " must be set");
      }
      if (klgeClientControllerModule == null) {
        throw new IllegalStateException(
            KlgeClientControllerModule.class.getCanonicalName() + " must be set");
      }
      if (appComponent == null) {
        throw new IllegalStateException(AppComponent.class.getCanonicalName() + " must be set");
      }
      return new DaggerMultiVideoChatFragmentComponent(this);
    }

    public Builder rtcContextModule(RtcContextModule rtcContextModule) {
      this.rtcContextModule = Preconditions.checkNotNull(rtcContextModule);
      return this;
    }

    public Builder roomMvpViewModule(RoomMvpViewModule roomMvpViewModule) {
      this.roomMvpViewModule = Preconditions.checkNotNull(roomMvpViewModule);
      return this;
    }

    public Builder klgeClientControllerModule(
        KlgeClientControllerModule klgeClientControllerModule) {
      this.klgeClientControllerModule = Preconditions.checkNotNull(klgeClientControllerModule);
      return this;
    }

    public Builder appComponent(AppComponent appComponent) {
      this.appComponent = Preconditions.checkNotNull(appComponent);
      return this;
    }
  }
}
