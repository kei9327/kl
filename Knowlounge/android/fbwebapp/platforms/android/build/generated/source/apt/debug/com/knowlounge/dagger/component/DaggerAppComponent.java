package com.knowlounge.dagger.component;

import android.app.Application;
import com.knowlounge.dagger.modules.AppModule;
import com.knowlounge.dagger.modules.AppModule_ProvideApplicationFactory;
import com.knowlounge.dagger.modules.AppModule_ProvideNavigatorFactory;
import com.knowlounge.view.room.Navigator;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerAppComponent implements AppComponent {
  private Provider<Application> provideApplicationProvider;

  private Provider<Navigator> provideNavigatorProvider;

  private DaggerAppComponent(Builder builder) {
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {

    this.provideApplicationProvider =
        DoubleCheck.provider(AppModule_ProvideApplicationFactory.create(builder.appModule));

    this.provideNavigatorProvider =
        DoubleCheck.provider(AppModule_ProvideNavigatorFactory.create(builder.appModule));
  }

  @Override
  public Application application() {
    return provideApplicationProvider.get();
  }

  @Override
  public Navigator navigator() {
    return provideNavigatorProvider.get();
  }

  public static final class Builder {
    private AppModule appModule;

    private Builder() {}

    public AppComponent build() {
      if (appModule == null) {
        throw new IllegalStateException(AppModule.class.getCanonicalName() + " must be set");
      }
      return new DaggerAppComponent(this);
    }

    public Builder appModule(AppModule appModule) {
      this.appModule = Preconditions.checkNotNull(appModule);
      return this;
    }
  }
}
