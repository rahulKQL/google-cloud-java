/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.data.v2.stub.mutaterows;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.core.SettableApiFuture;
import com.google.api.gax.batching.v2.RequestBuilder;
import com.google.api.gax.grpc.GrpcStatusCode;
import com.google.api.gax.rpc.DeadlineExceededException;
import com.google.api.gax.rpc.UnavailableException;
import com.google.cloud.bigtable.data.v2.internal.RequestContext;
import com.google.cloud.bigtable.data.v2.models.BulkMutation;
import com.google.cloud.bigtable.data.v2.models.MutateRowsException;
import com.google.cloud.bigtable.data.v2.models.Mutation;
import com.google.cloud.bigtable.data.v2.models.RowMutationEntry;
import com.google.common.collect.Lists;
import io.grpc.Status;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MutateRowsBatchingDescriptorV2Test {
  private static final String PROJECT_ID = "fake-project";
  private static final String INSTANCE_ID = "fake-instance";
  private static final String TABLE_ID = "fake-table";
  private static final String ROW_KEY = "fake-row-key";
  private static final String FAMILY = "fake-family";
  private static final String QUALIFIER = "fake-qualifier";
  private static final String VALUE = "fake-value";

  private static final RequestContext requestContext =
      RequestContext.create(PROJECT_ID, INSTANCE_ID, "");

  private MutateRowsBatchingDescriptorV2 descriptor;

  @Before
  public void setUp() {
    descriptor = new MutateRowsBatchingDescriptorV2();
  }

  @Test
  public void countBytesTest() {
    RowMutationEntry request = RowMutationEntry.create(ROW_KEY).setCell(FAMILY, QUALIFIER, VALUE);
    long bytes = request.toProto().getSerializedSize();
    assertThat(descriptor.countBytes(request)).isEqualTo(bytes);
  }

  @Test
  public void requestBuilderTest() {
    long timestamp = 10_000L;
    BulkMutation bulkMutation = BulkMutation.create(TABLE_ID);
    RequestBuilder<RowMutationEntry, BulkMutation> requestBuilder =
        descriptor.newRequestBuilder(bulkMutation);
    requestBuilder.add(
        RowMutationEntry.create(ROW_KEY).setCell(FAMILY, QUALIFIER, timestamp, VALUE));
    requestBuilder.add(
        RowMutationEntry.create("rowKey-2").setCell("family-2", "q", 20_000L, "some-value"));

    BulkMutation actualBulkMutation = requestBuilder.build();
    assertThat(actualBulkMutation.toProto(requestContext))
        .isEqualTo(
            BulkMutation.create(TABLE_ID)
                .add(ROW_KEY, Mutation.create().setCell(FAMILY, QUALIFIER, timestamp, VALUE))
                .add("rowKey-2", Mutation.create().setCell("family-2", "q", 20_000L, "some-value"))
                .toProto(requestContext));
  }

  @Test
  public void splitResponseTest() {
    List<SettableApiFuture<Void>> batchResponse =
        Lists.newArrayList(SettableApiFuture.<Void>create(), SettableApiFuture.<Void>create());

    assertThat(batchResponse.get(0).isDone()).isFalse();
    assertThat(batchResponse.get(1).isDone()).isFalse();
    descriptor.splitResponse(null, batchResponse);
    assertThat(batchResponse.get(0).isDone()).isTrue();
    assertThat(batchResponse.get(1).isDone()).isTrue();
  }

  @Test
  public void splitExceptionTest() {
    final RuntimeException expectedEx = new RuntimeException("Caused while batching");
    List<SettableApiFuture<Void>> batchResponses =
        Lists.newArrayList(SettableApiFuture.<Void>create(), SettableApiFuture.<Void>create());
    descriptor.splitException(expectedEx, batchResponses);

    for (SettableApiFuture fu : batchResponses) {
      try {
        fu.get();
      } catch (ExecutionException | InterruptedException ex) {
        assertThat(ex).hasCauseThat().isSameInstanceAs(expectedEx);
      }
    }
  }

  @Test
  public void splitExceptionWithFailedMutationsTest() {
    Throwable actualThrowable = null;
    SettableApiFuture<Void> response1 = SettableApiFuture.create();
    SettableApiFuture<Void> response2 = SettableApiFuture.create();
    SettableApiFuture<Void> response3 = SettableApiFuture.create();

    MutateRowsException serverError =
        new MutateRowsException(
            null,
            Lists.newArrayList(
                MutateRowsException.FailedMutation.create(
                    0,
                    new UnavailableException(
                        null, GrpcStatusCode.of(Status.Code.UNAVAILABLE), true)),
                MutateRowsException.FailedMutation.create(
                    1,
                    new DeadlineExceededException(
                        null, GrpcStatusCode.of(Status.Code.DEADLINE_EXCEEDED), true))),
            true);
    descriptor.splitException(serverError, Lists.newArrayList(response1, response2, response3));

    try {
      response1.get();
    } catch (ExecutionException | InterruptedException e) {
      actualThrowable = e;
    }
    assertThat(actualThrowable)
        .hasCauseThat()
        .isEqualTo(serverError.getFailedMutations().get(0).getError());

    actualThrowable = null;

    try {
      response2.get();
    } catch (ExecutionException | InterruptedException e) {
      actualThrowable = e;
    }
    assertThat(actualThrowable)
        .hasCauseThat()
        .isEqualTo(serverError.getFailedMutations().get(1).getError());

    try {
      response3.get();
    } catch (ExecutionException | InterruptedException e) {
      actualThrowable = e;
    }
    // The third response should has the last found failed mutation error.
    assertThat(actualThrowable)
        .hasCauseThat()
        .isEqualTo(serverError.getFailedMutations().get(1).getError());
  }
}
