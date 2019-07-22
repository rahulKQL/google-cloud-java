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
import com.google.cloud.bigtable.data.v2.models.BulkMutation;
import com.google.cloud.bigtable.data.v2.models.RowMutationEntry;
import java.util.List;

public class MutateRowsBatchingDescriptorV2
    implements BatchingDescriptor<RowMutationEntry, Void, BulkMutation, Void> {

  @Override
  public RequestBuilder<RowMutationEntry, BulkMutation> newRequestBuilder(
      BulkMutation bulkMutation) {
    return new MyRequestBuilder(bulkMutation);
  }

  @Override
  public void splitResponse(Void som, List<SettableApiFuture<Void>> list) {
    for (SettableApiFuture<Void> batchResponse : list) {
      batchResponse.set(null);
    }
  }

  @Override
  public void splitException(Throwable throwable, List<SettableApiFuture<Void>> list) {
    for (SettableApiFuture<Void> batchResponse : list) {
      batchResponse.setException(throwable);
    }
  }

  /** {@inheritDoc} */
  public long countBytes(RowMutationEntry request) {
    return request.toProto().getSerializedSize();
  }

  /** A {@link com.google.api.gax.batching.RequestBuilder} that can aggregate MutateRowsRequest */
  static class MyRequestBuilder implements RequestBuilder<RowMutationEntry, BulkMutation> {
    private BulkMutation bulkMutation;

    MyRequestBuilder(BulkMutation prototype) {
      this.bulkMutation = prototype.clone();
    }

    @Override
    public void add(RowMutationEntry rowEntry) {
      bulkMutation.add(rowEntry.getKey(), rowEntry.getMutation());
    }

    @Override
    public BulkMutation build() {
      return bulkMutation;
    }
  }
}
