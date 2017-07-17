package com.knowlounge.dagger.component;

import android.app.Activity;
import com.knowlounge.base.BaseActivity;
import com.knowlounge.base.BaseFragment;
import com.knowlounge.dagger.modules.ActivityModule;
import com.knowlounge.dagger.modules.ActivityModule_ProvideActivityFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.MembersInjectors;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerActivityComponent implements ActivityComponent {
  private Provider<Activity> provideActivityProvider;

  private DaggerActivityComponent(Builder builder) {
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {

    this.provideActivityProvider =
        DoubleCheck.provider(ActivityModule_ProvideActivityFactory.create(builder.activityModule));
  }

  @Override
  public void inject(BaseActivity activity) {
    MembersInjectors.<BaseActivity>noOp().injectMembers(activity);
  }

  @Override
  public void inject(BaseFragment fragment) {
    MembersInjectors.<BaseFragment>noOp().injectMembers(fragment);
  }

  @Override
  public Activity activity() {
    return provideActivityProvider.get();
  }

  public static final class Builder {
    private ActivityModule activityModule;

    private AppComponent appComponent;

    private Builder() {}

    public ActivityComponent build() {
      if (activityModule == null) {
        throw new IllegalStateException(ActivityModule.class.getCanonicalName() + " must be set");
      }
      if (appComponent == null) {
        throw new IllegalStateException(AppComponent.class.getCanonicalName() + " must be set");
      }
      return new DaggerActivityComponent(this);
    }

    public Builder activityModule(ActivityModule activityModule) {
      this.activityModule = Preconditions.checkNotNull(activityModule);
      return this;
    }

    public Builder appComponent(AppComponent appComponent) {
      this.appComponent = Preconditions.checkNotNull(appComponent);
      return this;
    }
  }
}
