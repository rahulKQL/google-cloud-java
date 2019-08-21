package com.google.cloud.logging;

import com.google.api.core.InternalApi;
import com.google.api.core.SettableApiFuture;
import com.google.api.gax.batching.BatchingDescriptor;
import com.google.api.gax.batching.BatchingRequestBuilder;
import com.google.cloud.logging.LogEntry;
import com.google.logging.v2.WriteLogEntriesRequest;
import com.google.logging.v2.WriteLogEntriesResponse;
import java.util.List;

@InternalApi("For internal use only")
public class LogBatchingDescriptor
    implements BatchingDescriptor<LogEntry, Void, WriteLogEntriesRequest, WriteLogEntriesResponse> {

  private final String projectId;

  public LogBatchingDescriptor(String projectId) {
    this.projectId = projectId;
  }

  @Override
  public BatchingRequestBuilder<LogEntry, WriteLogEntriesRequest> newRequestBuilder(
      WriteLogEntriesRequest writeLogEntriesRequest) {
    return new MyRequestBuilder(writeLogEntriesRequest);
  }

  @Override
  public void splitResponse(
      WriteLogEntriesResponse writeResponse, List<SettableApiFuture<Void>> batch) {
    for (SettableApiFuture<Void> batchResponse : batch) {
      batchResponse.set(null);
    }
  }

  @Override
  public void splitException(Throwable throwable, List<SettableApiFuture<Void>> batch) {
    // TODO(rahulkql): Find out different scenarios to cover in case of exception.
    for (SettableApiFuture<Void> batchResponse : batch) {
      batchResponse.setException(throwable);
    }
  }

  @Override
  public long countBytes(LogEntry logEntry) {
    return logEntry.toPb(projectId).getSerializedSize();
  }

  class MyRequestBuilder implements BatchingRequestBuilder<LogEntry, WriteLogEntriesRequest> {

    private final WriteLogEntriesRequest.Builder writeLogEntriesRequest;

    MyRequestBuilder(WriteLogEntriesRequest writeLogEntriesRequest) {
      this.writeLogEntriesRequest = writeLogEntriesRequest.toBuilder();
    }

    @Override
    public void add(LogEntry logEntry) {
      writeLogEntriesRequest.addEntries(logEntry.toPb(projectId));
    }

    @Override
    public WriteLogEntriesRequest build() {
      return writeLogEntriesRequest.build();
    }
  }
}
