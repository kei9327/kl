package com.knowlounge.dagger.component;

import android.app.Activity;
import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.apprtc.KlgePeerWatcher_Factory;
import com.knowlounge.dagger.modules.ActivityModule;
import com.knowlounge.dagger.modules.ActivityModule_ProvideActivityFactory;
import com.knowlounge.dagger.modules.RoomSpecModule;
import com.knowlounge.dagger.modules.RoomSpecModule_ProvideRoomSpecFactory;
import com.knowlounge.dagger.modules.RoomSpecModule_ProvideUserNoFactory;
import com.knowlounge.dagger.modules.RoomUsersModule;
import com.knowlounge.dagger.modules.RoomUsersModule_ProvideRoomUsersFactory;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.model.RoomUsers;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.view.room.RoomActivity_MembersInjector;
import com.knowlounge.view.room.RoomUserPresenter;
import com.knowlounge.view.room.RoomUserPresenter_Factory;
import dagger.MembersInjector;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.MembersInjectors;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerRoomActivityComponent implements RoomActivityComponent {
  private Provider<String> provideUserNoProvider;

  private Provider<KlgePeerWatcher> klgePeerWatcherProvider;

  private Provider<RoomActivityComponent> roomActivityComponentProvider;

  private Provider<RoomUserPresenter> roomUserPresenterProvider;

  private MembersInjector<RoomActivity> roomActivityMembersInjector;

  private Provider<Activity> provideActivityProvider;

  private Provider<RoomSpec> provideRoomSpecProvider;

  private Provider<RoomUsers> provideRoomUsersProvider;

  private DaggerRoomActivityComponent(Builder builder) {
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {

    this.provideUserNoProvider =
        DoubleCheck.provider(RoomSpecModule_ProvideUserNoFactory.create(builder.roomSpecModule));

    this.klgePeerWatcherProvider =
        DoubleCheck.provider(KlgePeerWatcher_Factory.create(provideUserNoProvider));

    this.roomActivityComponentProvider = InstanceFactory.<RoomActivityComponent>create(this);

    this.roomUserPresenterProvider =
        DoubleCheck.provider(
            RoomUserPresenter_Factory.create(
                MembersInjectors.<RoomUserPresenter>noOp(), roomActivityComponentProvider));

    this.roomActivityMembersInjector =
        RoomActivity_MembersInjector.create(klgePeerWatcherProvider, roomUserPresenterProvider);

    this.provideActivityProvider =
        DoubleCheck.provider(ActivityModule_ProvideActivityFactory.create(builder.activityModule));

    this.provideRoomSpecProvider =
        DoubleCheck.provider(RoomSpecModule_ProvideRoomSpecFactory.create(builder.roomSpecModule));

    this.provideRoomUsersProvider =
        DoubleCheck.provider(
            RoomUsersModule_ProvideRoomUsersFactory.create(builder.roomUsersModule));
  }

  @Override
  public void inject(RoomActivity activity) {
    roomActivityMembersInjector.injectMembers(activity);
  }

  @Override
  public Activity activity() {
    return provideActivityProvider.get();
  }

  @Override
  public RoomSpec getRoomSpec() {
    return provideRoomSpecProvider.get();
  }

  @Override
  public RoomUsers getRoomUsers() {
    return provideRoomUsersProvider.get();
  }

  public static final class Builder {
    private RoomSpecModule roomSpecModule;

    private ActivityModule activityModule;

    private RoomUsersModule roomUsersModule;

    private AppComponent appComponent;

    private Builder() {}

    public RoomActivityComponent build() {
      if (roomSpecModule == null) {
        throw new IllegalStateException(RoomSpecModule.class.getCanonicalName() + " must be set");
      }
      if (activityModule == null) {
        throw new IllegalStateException(ActivityModule.class.getCanonicalName() + " must be set");
      }
      if (roomUsersModule == null) {
        throw new IllegalStateException(RoomUsersModule.class.getCanonicalName() + " must be set");
      }
      if (appComponent == null) {
        throw new IllegalStateException(AppComponent.class.getCanonicalName() + " must be set");
      }
      return new DaggerRoomActivityComponent(this);
    }

    public Builder activityModule(ActivityModule activityModule) {
      this.activityModule = Preconditions.checkNotNull(activityModule);
      return this;
    }

    public Builder roomSpecModule(RoomSpecModule roomSpecModule) {
      this.roomSpecModule = Preconditions.checkNotNull(roomSpecModule);
      return this;
    }

    public Builder roomUsersModule(RoomUsersModule roomUsersModule) {
      this.roomUsersModule = Preconditions.checkNotNull(roomUsersModule);
      return this;
    }

    public Builder appComponent(AppComponent appComponent) {
      this.appComponent = Preconditions.checkNotNull(appComponent);
      return this;
    }
  }
}
