/*
 * Copyright 2015 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hannesdorfmann.mosby.mvp.rx.lce;

import com.hannesdorfmann.mosby.mvp.lce.MvpLceView;
import com.hannesdorfmann.mosby.mvp.rx.MvpBaseRxPresenter;
import com.hannesdorfmann.mosby.mvp.rx.lce.scheduler.AndroidSchedulerTransformer;
import rx.Observable;

/**
 * A presenter for RxJava, that assumes that only one Observable is subscribed by this presenter.
 * The idea is, that you make your (chain of) Observable and pass it to {@link
 * #subscribe(Observable, boolean)}. The presenter subscribes himself as Subscriber to the
 * observable
 * (which executes the observable). Before subscribing the presenter calls
 * {@link #applyScheduler(Observable)} to apply the <code>subscribeOn()</code> and
 * <code>observeOn()</code>
 *
 * @author Hannes Dorfmann
 * @since 1.0.0
 */
public abstract class MvpLceRxPresenter<V extends MvpLceView<M>, M>
    extends MvpBaseRxPresenter<V, M> {

  // TODO find a better way for pull to refresh
  private boolean pullToRefresh = false;

  /**
   * Subscribes the presenter himself as subscriber on the observable
   *
   * @param observable The observable to subscribe
   * @param pullToRefresh Pull to refresh?
   */
  public void subscribe(Observable<M> observable, boolean pullToRefresh) {
    this.pullToRefresh = pullToRefresh;
    observable = applyScheduler(observable);
    observable.subscribe(this);
  }

  /**
   * Called in {@link #subscribe(Observable, boolean)} to set  <code>subscribeOn()</code> and
   * <code>observeOn()</code>. As default it uses {@link AndroidSchedulerTransformer}. Override this
   * method if you want to provide your own scheduling implementation.
   */
  protected Observable<M> applyScheduler(Observable<M> observable) {
    return observable.compose(new AndroidSchedulerTransformer<M>());
  }

  @Override public void onCompleted() {
    if (isViewAttached()) {
      getView().showContent();
    }
  }

  @Override public void onError(Throwable e) {
    if (isViewAttached()) {
      getView().showError(e, pullToRefresh);
    }
  }

  @Override public void onNext(M data) {
    if (isViewAttached()) {
      getView().setData(data);
    }
  }
}
