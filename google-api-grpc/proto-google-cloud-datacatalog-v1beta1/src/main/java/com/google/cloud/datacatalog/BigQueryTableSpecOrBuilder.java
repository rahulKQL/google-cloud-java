// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/cloud/datacatalog/v1beta1/table_spec.proto

package com.google.cloud.datacatalog;

public interface BigQueryTableSpecOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.cloud.datacatalog.v1beta1.BigQueryTableSpec)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * The table source type.
   * </pre>
   *
   * <code>.google.cloud.datacatalog.v1beta1.TableSourceType table_source_type = 1;</code>
   */
  int getTableSourceTypeValue();
  /**
   *
   *
   * <pre>
   * The table source type.
   * </pre>
   *
   * <code>.google.cloud.datacatalog.v1beta1.TableSourceType table_source_type = 1;</code>
   */
  com.google.cloud.datacatalog.TableSourceType getTableSourceType();

  /**
   *
   *
   * <pre>
   * Table view specification. This field should only be populated if
   * table_source_type is BIGQUERY_VIEW.
   * </pre>
   *
   * <code>.google.cloud.datacatalog.v1beta1.ViewSpec view_spec = 2;</code>
   */
  boolean hasViewSpec();
  /**
   *
   *
   * <pre>
   * Table view specification. This field should only be populated if
   * table_source_type is BIGQUERY_VIEW.
   * </pre>
   *
   * <code>.google.cloud.datacatalog.v1beta1.ViewSpec view_spec = 2;</code>
   */
  com.google.cloud.datacatalog.ViewSpec getViewSpec();
  /**
   *
   *
   * <pre>
   * Table view specification. This field should only be populated if
   * table_source_type is BIGQUERY_VIEW.
   * </pre>
   *
   * <code>.google.cloud.datacatalog.v1beta1.ViewSpec view_spec = 2;</code>
   */
  com.google.cloud.datacatalog.ViewSpecOrBuilder getViewSpecOrBuilder();
}