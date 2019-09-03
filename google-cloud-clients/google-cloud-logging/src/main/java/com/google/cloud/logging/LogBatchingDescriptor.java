package com.google.cloud.logging;

import static com.google.cloud.logging.Logging.EntryListOption.OptionType.FILTER;
import static com.google.cloud.logging.Logging.EntryListOption.OptionType.ORDER_BY;
import static com.google.cloud.logging.Logging.ListOption.OptionType.PAGE_SIZE;
import static com.google.cloud.logging.Logging.ListOption.OptionType.PAGE_TOKEN;
import static com.google.cloud.logging.Logging.WriteOption.OptionType.LABELS;
import static com.google.cloud.logging.Logging.WriteOption.OptionType.LOG_NAME;
import static com.google.cloud.logging.Logging.WriteOption.OptionType.RESOURCE;

import com.google.api.core.InternalApi;
import com.google.api.core.SettableApiFuture;
import com.google.api.gax.batching.BatchingDescriptor;
import com.google.api.gax.batching.BatchingRequestBuilder;
import com.google.cloud.MonitoredResource;
import com.google.logging.v2.ProjectLogName;
import com.google.logging.v2.WriteLogEntriesRequest;
import com.google.logging.v2.WriteLogEntriesResponse;
import java.util.List;
import java.util.Map;

@InternalApi("For internal use only")
public class LogBatchingDescriptor
    implements BatchingDescriptor<
        LogEntryWithOption, Void, WriteLogEntriesRequest, WriteLogEntriesResponse> {

  private final String projectId;

  public LogBatchingDescriptor(String projectId) {
    this.projectId = projectId;
  }

  @Override
  public BatchingRequestBuilder<LogEntryWithOption, WriteLogEntriesRequest> newRequestBuilder(
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
  public long countBytes(LogEntryWithOption logEntryWithOption) {
    long size = 0;
    for (LogEntry logE : logEntryWithOption.getLogEntries()) {
      size += logE.toPb(projectId).getSerializedSize();
    }
    return size;
  }

  class MyRequestBuilder
      implements BatchingRequestBuilder<LogEntryWithOption, WriteLogEntriesRequest> {

    private final WriteLogEntriesRequest.Builder builder;

    MyRequestBuilder(WriteLogEntriesRequest writeLogEntriesRequest) {
      this.builder = writeLogEntriesRequest.toBuilder();
    }

    @Override
    public void add(LogEntryWithOption logEntryWithOption) {
      Map<Option.OptionType, ?> optionTypeMap = null;

      String logName = LOG_NAME.get(optionTypeMap);
      if (logName != null) {
        builder.setLogName(ProjectLogName.of(projectId, logName).toString());
      }

      MonitoredResource resource = RESOURCE.get(optionTypeMap);
      if (resource != null) {
        builder.setResource(resource.toPb());
      }

      Map<String, String> labels = LABELS.get(optionTypeMap);
      if (labels != null) {
        builder.putAllLabels(labels);
      }

      for (LogEntry logE : logEntryWithOption.getLogEntries()) {
        builder.addEntries(logE.toPb(projectId));
      }
    }

    @Override
    public WriteLogEntriesRequest build() {
      return builder.build();
    }
  }
}
