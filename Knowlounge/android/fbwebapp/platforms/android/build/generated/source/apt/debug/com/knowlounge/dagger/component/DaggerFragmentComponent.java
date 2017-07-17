package com.knowlounge.dagger.component;

import android.support.v4.app.Fragment;
import com.knowlounge.base.BaseFragment;
import com.knowlounge.dagger.modules.FragmentModule;
import com.knowlounge.dagger.modules.FragmentModule_ProvideFragmentFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.MembersInjectors;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerFragmentComponent implements FragmentComponent {
  private Provider<Fragment> provideFragmentProvider;

  private DaggerFragmentComponent(Builder builder) {
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {

    this.provideFragmentProvider =
        DoubleCheck.provider(FragmentModule_ProvideFragmentFactory.create(builder.fragmentModule));
  }

  @Override
  public void inject(BaseFragment fragment) {
    MembersInjectors.<BaseFragment>noOp().injectMembers(fragment);
  }

  @Override
  public Fragment fragment() {
    return provideFragmentProvider.get();
  }

  public static final class Builder {
    private FragmentModule fragmentModule;

    private AppComponent appComponent;

    private Builder() {}

    public FragmentComponent build() {
      if (fragmentModule == null) {
        throw new IllegalStateException(FragmentModule.class.getCanonicalName() + " must be set");
      }
      if (appComponent == null) {
        throw new IllegalStateException(AppComponent.class.getCanonicalName() + " must be set");
      }
      return new DaggerFragmentComponent(this);
    }

    public Builder fragmentModule(FragmentModule fragmentModule) {
      this.fragmentModule = Preconditions.checkNotNull(fragmentModule);
      return this;
    }

    public Builder appComponent(AppComponent appComponent) {
      this.appComponent = Preconditions.checkNotNull(appComponent);
      return this;
    }
  }
}
