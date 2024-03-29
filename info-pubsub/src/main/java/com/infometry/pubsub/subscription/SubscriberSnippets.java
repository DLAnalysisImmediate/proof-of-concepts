package com.infometry.pubsub.subscription;

import com.google.api.core.ApiFuture;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriberSnippets {

	private final ProjectSubscriptionName subscriptionName;

	private final MessageReceiver receiver;

	private final ApiFuture<Void> done;

	public SubscriberSnippets(ProjectSubscriptionName subscriptionName, MessageReceiver receiver, ApiFuture<Void> done) 
	{
		this.subscriptionName = subscriptionName;
		this.receiver = receiver;
		this.done = done;
	}

	public void startAndWait() throws Exception {
		Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
		ExecutorService pool = Executors.newCachedThreadPool();
		subscriber.addListener(
				new Subscriber.Listener() {
					public void failed(Subscriber.State from, Throwable failure) {
						// Handle error.
					}
				},
				pool);
		subscriber.startAsync();
		done.get();
		pool.shutdown();
		subscriber.stopAsync().awaitTerminated();
	}

	@SuppressWarnings("unused")
	private void createSubscriber() throws Exception {
		
		String projectId = "my-project-id";
		String subscriptionId = "my-subscription-id";

		ProjectSubscriptionName subscriptionName =
				ProjectSubscriptionName.of(projectId, subscriptionId);
		// Instantiate an asynchronous message receiver
		MessageReceiver receiver =
				new MessageReceiver() {
			@Override
			public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
				// handle incoming message, then ack/nack the received message
				System.out.println("Id : " + message.getMessageId());
				System.out.println("Data : " + message.getData().toStringUtf8());
				consumer.ack();
			}
		};

		Subscriber subscriber = null;
		try {
			// Create a subscriber for "my-subscription-id" bound to the message receiver
			subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
			subscriber.startAsync().awaitRunning();
			// Allow the subscriber to run indefinitely unless an unrecoverable error occurs
			subscriber.awaitTerminated();
		} finally {
			// Stop receiving messages
			if (subscriber != null) {
				subscriber.stopAsync();
			}
		}
		// [END pubsub_subscriber_async_pull]
	}

	@SuppressWarnings("unused")
	private Subscriber createSubscriberWithErrorListener(Subscriber subscriber) throws Exception {
		// [START pubsub_subscriber_error_listener]
		subscriber.addListener(
				new Subscriber.Listener() {
					public void failed(Subscriber.State from, Throwable failure) {
						// Handle error.
					}
				},
				MoreExecutors.directExecutor());
		// [END pubsub_subscriber_error_listener]
		return subscriber;
	}

	@SuppressWarnings("unused")
	private Subscriber createSingleThreadedSubscriber() throws Exception {
		// [START pubsub_subscriber_concurrency_control]
		// provide a separate executor service for polling
		ExecutorProvider executorProvider =
				InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(1).build();

		Subscriber subscriber =
				Subscriber.newBuilder(subscriptionName, receiver)
				.setExecutorProvider(executorProvider)
				.build();
		// [END pubsub_subscriber_concurrency_control]
		return subscriber;
	}

	@SuppressWarnings("unused")
	private Subscriber createSubscriberWithCustomFlowSettings() throws Exception {
		// [START pubsub_subscriber_flow_settings]
		FlowControlSettings flowControlSettings =
				FlowControlSettings.newBuilder()
				.setMaxOutstandingElementCount(10_000L)
				.setMaxOutstandingRequestBytes(1_000_000_000L)
				.build();
		Subscriber subscriber =
				Subscriber.newBuilder(subscriptionName, receiver)
				.setFlowControlSettings(flowControlSettings)
				.build();
		// [END pubsub_subscriber_flow_settings]
		return subscriber;
	}

	@SuppressWarnings("unused")
	private Subscriber createSubscriberWithCustomCredentials() throws Exception {
		// [START pubsub_subscriber_custom_credentials]
		CredentialsProvider credentialsProvider =
				FixedCredentialsProvider.create(
						ServiceAccountCredentials.fromStream(new FileInputStream("credentials.json")));

		Subscriber subscriber =
				Subscriber.newBuilder(subscriptionName, receiver)
				.setCredentialsProvider(credentialsProvider)
				.build();
		// [END pubsub_subscriber_custom_credentials]
		return subscriber;
	}

	static List<ReceivedMessage> createSubscriberWithSyncPull(
			String projectId, String subscriptionId, int numOfMessages) throws Exception {
		// [START pubsub_subscriber_sync_pull]
		SubscriberStubSettings subscriberStubSettings =
				SubscriberStubSettings.newBuilder()
				.setTransportChannelProvider(
						SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
						.setMaxInboundMessageSize(20 << 20) // 20MB
						.build())
				.build();

		try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
			// String projectId = "my-project-id";
			// String subscriptionId = "my-subscription-id";
			// int numOfMessages = 10;   // max number of messages to be pulled
			String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
			PullRequest pullRequest =
					PullRequest.newBuilder()
					.setMaxMessages(numOfMessages)
					.setReturnImmediately(false) // return immediately if messages are not available
					.setSubscription(subscriptionName)
					.build();

			// use pullCallable().futureCall to asynchronously perform this operation
			PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
			List<String> ackIds = new ArrayList<>();
			for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
				// handle received message
				// ...
				ackIds.add(message.getAckId());
			}
			// acknowledge received messages
			AcknowledgeRequest acknowledgeRequest =
					AcknowledgeRequest.newBuilder()
					.setSubscription(subscriptionName)
					.addAllAckIds(ackIds)
					.build();
			// use acknowledgeCallable().futureCall to asynchronously perform this operation
			subscriber.acknowledgeCallable().call(acknowledgeRequest);
			return pullResponse.getReceivedMessagesList();
		}
		// [END pubsub_subscriber_sync_pull]
	}
}
