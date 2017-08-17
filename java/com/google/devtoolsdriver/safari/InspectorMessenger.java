// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtoolsdriver.safari;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.base.VerifyException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.iosdevicecontrol.util.FluentLogger;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.iosdevicecontrol.util.EllipsisFormat;
import com.google.iosdevicecontrol.webinspector.ApplicationConnectedMessage;
import com.google.iosdevicecontrol.webinspector.ApplicationDisconnectedMessage;
import com.google.iosdevicecontrol.webinspector.ApplicationSentDataMessage;
import com.google.iosdevicecontrol.webinspector.ApplicationSentListingMessage;
import com.google.iosdevicecontrol.webinspector.ApplicationUpdatedMessage;
import com.google.iosdevicecontrol.webinspector.ForwardGetListingMessage;
import com.google.iosdevicecontrol.webinspector.ForwardSocketDataMessage;
import com.google.iosdevicecontrol.webinspector.ForwardSocketSetupMessage;
import com.google.iosdevicecontrol.webinspector.InspectorApplication;
import com.google.iosdevicecontrol.webinspector.InspectorDriver;
import com.google.iosdevicecontrol.webinspector.InspectorMessage;
import com.google.iosdevicecontrol.webinspector.InspectorPage;
import com.google.iosdevicecontrol.webinspector.ReportConnectedApplicationListMessage;
import com.google.iosdevicecontrol.webinspector.ReportConnectedDriverListMessage;
import com.google.iosdevicecontrol.webinspector.ReportIdentifierMessage;
import com.google.iosdevicecontrol.webinspector.WebInspector;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.concurrent.GuardedBy;
import javax.json.JsonObject;

/** The state of the applications available and their page listings. */
final class InspectorMessenger implements Closeable {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /** Used to truncate inspector messages in the log. */
  private static final EllipsisFormat MESSAGE_FORMAT = new EllipsisFormat(2500);

  /** The sender value seems to be an arbitrary UUID, so choose a fixed one here. */
  private static final String SENDER_UUID = UUID.randomUUID().toString();

  private final WebInspector inspector;
  private final String connectionId = UUID.randomUUID().toString();
  private final AtomicReference<Consumer<JsonObject>> devtoolsListener = new AtomicReference<>();
  private final ScheduledExecutorService executor;
  private final Future<?> receiveFuture;
  private final Monitor monitor = new Monitor();

  @GuardedBy("monitor")
  private final BiMap<String, AppListing> appIdToListings = HashBiMap.create();

  @GuardedBy("this")
  private Optional<PageContext> activePageContext = Optional.empty();

  InspectorMessenger(WebInspector inspector) {
    this(inspector, Executors.newSingleThreadScheduledExecutor());
  }

  @VisibleForTesting
  InspectorMessenger(WebInspector inspector, ScheduledExecutorService executor) {
    this.inspector = checkNotNull(inspector);
    this.executor = checkNotNull(executor);
    receiveFuture = executor.scheduleWithFixedDelay(this::receiveMessage, 0, 50, MILLISECONDS);
  }

  void setEventListener(Consumer<JsonObject> listener) {
    devtoolsListener.set(checkNotNull(listener));
  }

  synchronized OptionalInt activePageId() {
    return activePageContext.isPresent()
        ? OptionalInt.of(activePageContext.get().pageId)
        : OptionalInt.empty();
  }

  void sendConnect() throws IOException {
    sendMessage(ReportIdentifierMessage.builder().connectionId(connectionId));
  }

  synchronized boolean sendSwitchTo(int pageId) throws IOException {
    PageContext activePageContext = checkActivePageContext();
    return sendSwitchTo(activePageContext.appId, pageId);
  }

  synchronized boolean sendSwitchTo(String appId, int pageId) throws IOException {
    // Important that a ForwardSocketSetup message is never sent to an already setup socket,
    // because this sometimes causes the application to disconnect, at least on iOS 9.
    if (activePageContext.isPresent()
        && activePageContext.get().appId.equals(appId)
        && activePageContext.get().pageId == pageId) {
      return false;
    }
    sendMessage(
        ForwardSocketSetupMessage.builder()
            .applicationId(appId)
            .automaticallyPause(false)
            .connectionId(connectionId)
            .pageId(pageId)
            .sender(SENDER_UUID));
    activePageContext = Optional.of(new PageContext(appId, pageId));
    return true;
  }

  synchronized void sendListPages() throws IOException {
    String activeAppId = checkActivePageContext().appId;
    monitor.enter();
    try {
      appIdToListings.computeIfPresent(
          activeAppId, (unused, appListing) -> new AppListing(appListing.app, Optional.empty()));
    } finally {
      monitor.leave();
    }
    sendMessage(
        ForwardGetListingMessage.builder().applicationId(activeAppId).connectionId(connectionId));
  }

  synchronized void sendCommand(JsonObject command) throws IOException {
    PageContext activePageContext = checkActivePageContext();
    checkNotNull(devtoolsListener.get());
    sendMessage(
        ForwardSocketDataMessage.builder()
            .applicationId(activePageContext.appId)
            .connectionId(connectionId)
            .pageId(activePageContext.pageId)
            .sender(SENDER_UUID)
            .socketData(command));
  }

  @VisibleForTesting
  @SuppressWarnings("GuardedBy")
  Optional<ImmutableSet<AppListing>> getAllAppListings(String hostBundleId) {
    Set<AppListing> listings = appIdToListings.values();
    ImmutableSet<String> hostAppIds =
        listings
            .stream()
            .filter(appListing -> appListing.app.applicationBundleId().equals(hostBundleId))
            .map(appListing -> appListing.app.applicationId())
            .collect(ImmutableSet.toImmutableSet());
    Verify.verify(hostAppIds.size() <= 1, "multiple matching host apps: %s", hostAppIds);
    if (!hostAppIds.isEmpty()) {
      String hostAppId = Iterables.getOnlyElement(hostAppIds);
      ImmutableSet<AppListing> childListings =
          listings
              .stream()
              .filter(
                  appListing ->
                      hostAppId.equals(appListing.app.optionalHostApplicationId().orNull()))
              .collect(ImmutableSet.toImmutableSet());
      if (!childListings.isEmpty()
          && childListings.stream().allMatch(appListing -> appListing.listing.isPresent())) {
        return Optional.of(childListings);
      }
    }
    return Optional.empty();
  }

  ImmutableList<InspectorPage> awaitPages() throws IOException {
    return await(this::getPages);
  }

  @VisibleForTesting
  @SuppressWarnings("GuardedBy")
  synchronized Optional<ImmutableList<InspectorPage>> getPages() {
    PageContext activePageContext = checkActivePageContext();
    AppListing appListing = appIdToListings.get(activePageContext.appId);
    return appListing == null ? Optional.empty() : appListing.listing;
  }

  ImmutableSet<AppListing> awaitAllAppListings(String hostBundleId) throws IOException {
    return await(() -> getAllAppListings(hostBundleId));
  }

  /** Waits for the supplier to return a present value. */
  private <T> T await(Supplier<Optional<T>> compute) throws IOException {
    AtomicReference<T> result = new AtomicReference<>();
    BooleanSupplier condition =
        () -> {
          Optional<T> value = compute.get();
          if (value.isPresent()) {
            result.set(value.get());
          }
          return value.isPresent();
        };
    try {
      monitor.enterWhen(monitor.newGuard(condition));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e);
    }
    try {
      return Verify.verifyNotNull(result.get());
    } finally {
      monitor.leave();
    }
  }

  /** Stops receiving messages from the inspector and closes it. */
  @Override
  public void close() throws IOException {
    try {
      // Canceling the future marks the future done and the messenger "closed".
      // If it can't be cancelled, it must have terminated prematurely, so raise an exception.
      if (!receiveFuture.cancel(false)) {
        Futures.getChecked(receiveFuture, IOException.class);
      }
    } finally {
      try {
        MoreExecutors.shutdownAndAwaitTermination(executor, 5, SECONDS);
      } finally {
        inspector.close();
      }
    }
  }

  /** A pair of an application and a page listing. */
  static final class AppListing {
    final InspectorApplication app;
    final Optional<ImmutableList<InspectorPage>> listing;

    private AppListing(InspectorApplication app, Optional<ImmutableList<InspectorPage>> listing) {
      this.app = app;
      this.listing = listing;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper("AppListing")
          .add("app", app)
          .add("listing", listing)
          .toString();
    }
  }

  private boolean isClosed() {
    return receiveFuture.isDone();
  }

  private void sendMessage(InspectorMessage.Builder builder) throws IOException {
    InspectorMessage message = builder.build();
    logger.atInfo().log("Message sent: %s", formatMessage(message));
    inspector.sendMessage(message);
  }

  private void receiveMessage() {
    try {
      // Receive a plist over the socket. On EOF, if the thread is marked interrupted, that means
      // the socket was intentionally closed by the #close method; otherwise do a full close now.
      Optional<InspectorMessage> message = inspector.receiveMessage();
      if (!message.isPresent()) {
        if (!isClosed()) {
          logger.atSevere().log("Web inspector closed unexpectedly.");
          close();
        }
        return;
      }

      onMessageReceived(message.get());
    } catch (Throwable e) {
      logger.atWarning().withCause(e).log();
    }
  }

  private void onMessageReceived(InspectorMessage message) {
    logger.atInfo().log("Message received: %s", formatMessage(message));
    switch (message.selector()) {
      case APPLICATION_CONNECTED:
        addApplication(((ApplicationConnectedMessage) message).asApplication());
        return;

      case APPLICATION_DISCONNECTED:
        String disconnectedId = ((ApplicationDisconnectedMessage) message).applicationId();
        synchronized (this) {
          if (activePageContext.isPresent()
              && disconnectedId.equals(activePageContext.get().appId)) {
            activePageContext = Optional.empty();
          }
        }
        monitor.enter();
        try {
          appIdToListings.remove(disconnectedId);
        } finally {
          monitor.leave();
        }
        return;

      case APPLICATION_SENT_DATA:
        devtoolsListener.get().accept(((ApplicationSentDataMessage) message).messageData());
        return;

      case APPLICATION_SENT_LISTING:
        ApplicationSentListingMessage listingMsg = (ApplicationSentListingMessage) message;
        String appId = listingMsg.applicationId();
        monitor.enter();
        try {
          AppListing curListing = appIdToListings.get(appId);
          Verify.verifyNotNull(curListing, "received listing for unknown app: %s", appId);
          AppListing newListing = new AppListing(curListing.app, Optional.of(listingMsg.listing()));
          appIdToListings.put(appId, newListing);
        } finally {
          monitor.leave();
        }
        return;

      case APPLICATION_UPDATED:
        addApplication(((ApplicationUpdatedMessage) message).asApplication());
        return;

      case REPORT_CONNECTED_APPLICATION_LIST:
        ImmutableList<InspectorApplication> apps =
            ((ReportConnectedApplicationListMessage) message).applicationDictionary();
        for (InspectorApplication app : apps) {
          addApplication(app);
        }
        return;

      case REPORT_CONNECTED_DRIVER_LIST:
        // We've never seen one of these messages before where the driver dictionary was
        // populated, nor do we know what it means, so let's be alerted the first time it happens.
        ImmutableList<InspectorDriver> drivers =
            ((ReportConnectedDriverListMessage) message).driverDictionary();
        Verify.verify(drivers.isEmpty());
        return;

      case REPORT_SETUP:
        // Intentionally ignore that the connection is setup.
        return;

      default:
        throw new VerifyException("Did not expect to receive message: " + message);
    }
  }

  private void addApplication(InspectorApplication app) {
    monitor.enter();
    try {
      appIdToListings.compute(
          app.applicationId(),
          (appId, appListing) ->
              new AppListing(app, appListing == null ? Optional.empty() : appListing.listing));
    } finally {
      monitor.leave();
    }
  }

  private synchronized PageContext checkActivePageContext() {
    checkState(activePageContext.isPresent());
    return activePageContext.get();
  }

  private static final class PageContext {
    private final String appId;
    private final int pageId;

    private PageContext(String appId, int pageId) {
      this.appId = checkNotNull(appId);
      this.pageId = pageId;
    }
  }

  private static Object formatMessage(InspectorMessage message) {
    return new Object() {
      @Override
      public String toString() {
        return MESSAGE_FORMAT.format(message.toString());
      }
    };
  }
}
