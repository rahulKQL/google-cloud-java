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

import com.google.api.core.SettableApiFuture;
import com.google.api.gax.batching.v2.BatchingDescriptor;
import com.google.api.gax.batching.v2.RequestBuilder;
import com.google.api.gax.rpc.ApiException;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.cloud.bigtable.data.v2.models.BulkMutation;
import com.google.cloud.bigtable.data.v2.models.MutateRowsException;
import com.google.cloud.bigtable.data.v2.models.MutateRowsException.FailedMutation;
import com.google.cloud.bigtable.data.v2.models.Mutation;
import com.google.cloud.bigtable.data.v2.models.RowMutationEntry;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A custom implementation of a {@link BatchingDescriptor} to split Rpc response into individual
 * rows response and in a {@link MutateRowsException}.
 *
 * <p>This class is considered an internal implementation detail and not meant to be used by
 * applications directly.
 */
public class MutateRowsBatchingDescriptorV2
    implements BatchingDescriptor<RowMutationEntry, Void, BulkMutation, Void> {

  @Override
  public RequestBuilder<RowMutationEntry, BulkMutation> newRequestBuilder(
      BulkMutation bulkMutation) {
    return new MyRequestBuilder(bulkMutation);
  }

  @Override
  public void splitResponse(Void som, List<SettableApiFuture<Void>> batch) {
    for (SettableApiFuture<Void> batchResponse : batch) {
      batchResponse.set(null);
    }
  }

  @Override
  public void splitException(Throwable throwable, List<SettableApiFuture<Void>> batch) {
    if (!(throwable instanceof MutateRowsException)) {
      for (SettableApiFuture<Void> future : batch) {
        future.setException(throwable);
      }
      return;
    }

    List<FailedMutation> failedMutations = ((MutateRowsException) throwable).getFailedMutations();

    Map<Integer, FailedMutation> errorsByIndex =
        Maps.uniqueIndex(
            failedMutations,
            new Function<FailedMutation, Integer>() {
              @Override
              public Integer apply(@Nullable FailedMutation input) {
                return input.getIndex();
              }
            });

    int i = 0;
    for (SettableApiFuture<Void> response : batch) {
      // NOTE: The gax batching api doesn't allow for a single response to get different
      // exception for different entries. However this does not affect this client because
      // SettableApiFuture corresponds to  single element. So just use the last error per entry.
      ApiException lastError = null;

      FailedMutation failure = errorsByIndex.get(i++);

      if (failure != null) {
        lastError = failure.getError();
      }

      if (lastError == null) {
        response.set(null);
      } else {
        response.setException(lastError);
      }
    }
  }

  /** {@inheritDoc} */
  public long countBytes(RowMutationEntry request) {
    return request.toProto().getSerializedSize();
  }

  /** A {@link RequestBuilder} that can aggregate BulkMutation */
  static class MyRequestBuilder implements RequestBuilder<RowMutationEntry, BulkMutation> {
    private BulkMutation bulkMutation;

    MyRequestBuilder(BulkMutation prototype) {
      this.bulkMutation = prototype.clone();
    }

    @Override
    public void add(RowMutationEntry rowEntry) {
      MutateRowsRequest.Entry entry = rowEntry.toProto();
      ByteString rowK = entry.getRowKey();
      bulkMutation.add(rowK, Mutation.fromProtoUnsafe(entry.getMutationsList()));
    }

    @Override
    public BulkMutation build() {
      return bulkMutation;
    }
  }
}
