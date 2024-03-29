package com.infometry.pubsub.snapshot;

import java.io.IOException;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.pubsub.v1.ProjectSnapshotName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.infometry.pubsub.PubSubConnection;
public class CreateSnapshot {
	public static void main(String[] args) {
		//String projectId = ServiceOptions.getDefaultProjectId();
		CredentialsProvider credProvider=PubSubConnection.getCredentials();
		String projectId=PubSubConnection.getProjectId();
		String snapshotId = "Sample2Snapshot";
		String subscriptionId = "Push-Subscription";
		try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder().setCredentialsProvider(credProvider).build())) {
			ProjectSnapshotName name = ProjectSnapshotName.of(projectId, snapshotId);
			ProjectSubscriptionName subscription = ProjectSubscriptionName.of(projectId, subscriptionId);
			subscriptionAdminClient.createSnapshot(name, subscription);
			System.out.println("Snapshot "+name.getSnapshot()+" created");
		} catch (InvalidArgumentException | IOException e) {
		}
	}
}
