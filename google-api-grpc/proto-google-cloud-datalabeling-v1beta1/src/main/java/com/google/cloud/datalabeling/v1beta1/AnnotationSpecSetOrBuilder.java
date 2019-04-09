// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/cloud/datalabeling/v1beta1/annotation_spec_set.proto

package com.google.cloud.datalabeling.v1beta1;

public interface AnnotationSpecSetOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.cloud.datalabeling.v1beta1.AnnotationSpecSet)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * Output only.
   * AnnotationSpecSet resource name, format:
   * projects/{project_id}/annotationSpecSets/{annotation_spec_set_id}
   * </pre>
   *
   * <code>string name = 1;</code>
   */
  java.lang.String getName();
  /**
   *
   *
   * <pre>
   * Output only.
   * AnnotationSpecSet resource name, format:
   * projects/{project_id}/annotationSpecSets/{annotation_spec_set_id}
   * </pre>
   *
   * <code>string name = 1;</code>
   */
  com.google.protobuf.ByteString getNameBytes();

  /**
   *
   *
   * <pre>
   * Required. The display name for AnnotationSpecSet defined by user.
   * Maximum of 64 characters.
   * </pre>
   *
   * <code>string display_name = 2;</code>
   */
  java.lang.String getDisplayName();
  /**
   *
   *
   * <pre>
   * Required. The display name for AnnotationSpecSet defined by user.
   * Maximum of 64 characters.
   * </pre>
   *
   * <code>string display_name = 2;</code>
   */
  com.google.protobuf.ByteString getDisplayNameBytes();

  /**
   *
   *
   * <pre>
   * Optional. User-provided description of the annotation specification set.
   * The description can be up to 10000 characters long.
   * </pre>
   *
   * <code>string description = 3;</code>
   */
  java.lang.String getDescription();
  /**
   *
   *
   * <pre>
   * Optional. User-provided description of the annotation specification set.
   * The description can be up to 10000 characters long.
   * </pre>
   *
   * <code>string description = 3;</code>
   */
  com.google.protobuf.ByteString getDescriptionBytes();

  /**
   *
   *
   * <pre>
   * Required. The actual spec set defined by the users.
   * </pre>
   *
   * <code>repeated .google.cloud.datalabeling.v1beta1.AnnotationSpec annotation_specs = 4;</code>
   */
  java.util.List<com.google.cloud.datalabeling.v1beta1.AnnotationSpec> getAnnotationSpecsList();
  /**
   *
   *
   * <pre>
   * Required. The actual spec set defined by the users.
   * </pre>
   *
   * <code>repeated .google.cloud.datalabeling.v1beta1.AnnotationSpec annotation_specs = 4;</code>
   */
  com.google.cloud.datalabeling.v1beta1.AnnotationSpec getAnnotationSpecs(int index);
  /**
   *
   *
   * <pre>
   * Required. The actual spec set defined by the users.
   * </pre>
   *
   * <code>repeated .google.cloud.datalabeling.v1beta1.AnnotationSpec annotation_specs = 4;</code>
   */
  int getAnnotationSpecsCount();
  /**
   *
   *
   * <pre>
   * Required. The actual spec set defined by the users.
   * </pre>
   *
   * <code>repeated .google.cloud.datalabeling.v1beta1.AnnotationSpec annotation_specs = 4;</code>
   */
  java.util.List<? extends com.google.cloud.datalabeling.v1beta1.AnnotationSpecOrBuilder>
      getAnnotationSpecsOrBuilderList();
  /**
   *
   *
   * <pre>
   * Required. The actual spec set defined by the users.
   * </pre>
   *
   * <code>repeated .google.cloud.datalabeling.v1beta1.AnnotationSpec annotation_specs = 4;</code>
   */
  com.google.cloud.datalabeling.v1beta1.AnnotationSpecOrBuilder getAnnotationSpecsOrBuilder(
      int index);
}