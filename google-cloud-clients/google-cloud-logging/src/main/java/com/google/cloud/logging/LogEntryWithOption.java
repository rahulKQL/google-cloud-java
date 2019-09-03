package com.google.cloud.logging;

import com.google.common.collect.ImmutableList;

public class LogEntryWithOption {

  private final Iterable<LogEntry> logEntries;
  private final Logging.WriteOption[] writeOptions;

  public Iterable<LogEntry> getLogEntries() {
    return logEntries;
  }

  public Logging.WriteOption[] getWriteOption() {
    return writeOptions;
  }

  LogEntryWithOption(Iterable<LogEntry> logEntries, Logging.WriteOption... writeOptions) {
    this.logEntries = ImmutableList.copyOf(logEntries);
    this.writeOptions = writeOptions;
  }
}
