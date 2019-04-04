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
    import com.google.bigtable.v2.MutateRowsRequest;
    import com.google.bigtable.v2.MutateRowsResponse;
    import java.util.List;

public class MutateRowsEntryBatchingDescriptor implements
    BatchingDescriptor<MutateRowsRequest.Entry, MutateRowsResponse.Entry, MutateRowsRequest,
        List<MutateRowsResponse>> {

  @Override
  public RequestBuilder<MutateRowsRequest.Entry, MutateRowsRequest> newRequestBuilder(
      final MutateRowsRequest prototype) {

    return new RequestBuilder<MutateRowsRequest.Entry, MutateRowsRequest>() {
      private final MutateRowsRequest.Builder builder = prototype.toBuilder();

      @Override
      public void add(MutateRowsRequest.Entry entry) {
        builder.addEntries(entry);
      }

      @Override
      public MutateRowsRequest build() {
        return builder.build();
      }
    };
  }

  @Override
  public void splitResponse(List<MutateRowsResponse> rows,
      List<SettableApiFuture<MutateRowsResponse.Entry>> list) {
    int i= 0;
    for(MutateRowsResponse response : rows){
      for(int j=0; j < response.getEntriesCount(); i++, j++){
        list.get(i).set(response.getEntries(j));
      }
    }
  }


  @Override
  public void splitException(Throwable throwable,
      List<SettableApiFuture<MutateRowsResponse.Entry>> list) {
    for (SettableApiFuture<MutateRowsResponse.Entry> entryFuture : list) {
      entryFuture.setException(throwable);
    }
  }

  @Override
  public long countBytes(MutateRowsRequest.Entry entry) {
    if (entry == null) {
      return 0;
    }
    return entry.getSerializedSize();
  }
}