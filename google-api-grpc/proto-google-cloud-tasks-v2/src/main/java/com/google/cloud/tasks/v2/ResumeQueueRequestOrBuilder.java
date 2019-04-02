// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/cloud/tasks/v2/cloudtasks.proto

package com.google.cloud.tasks.v2;

public interface ResumeQueueRequestOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.cloud.tasks.v2.ResumeQueueRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * Required.
   * The queue name. For example:
   * `projects/PROJECT_ID/location/LOCATION_ID/queues/QUEUE_ID`
   * </pre>
   *
   * <code>string name = 1;</code>
   */
  java.lang.String getName();
  /**
   *
   *
   * <pre>
   * Required.
   * The queue name. For example:
   * `projects/PROJECT_ID/location/LOCATION_ID/queues/QUEUE_ID`
   * </pre>
   *
   * <code>string name = 1;</code>
   */
  com.google.protobuf.ByteString getNameBytes();
}
