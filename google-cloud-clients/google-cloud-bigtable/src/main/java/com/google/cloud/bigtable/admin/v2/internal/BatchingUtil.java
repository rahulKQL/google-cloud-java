package com.google.cloud.bigtable.admin.v2.internal;

import com.google.api.core.InternalApi;
import com.google.api.gax.batching.BatchingSettings;

/**
 * Internal helper to interchange BatchingSettings versions.
 *
 * <p>This class is considered an internal implementation detail and not meant to be used by
 * applications.
 */
@InternalApi
public final class BatchingUtil {

  public static BatchingSettings toV1(
      com.google.api.gax.batching.v2.BatchingSettings batchingSettings) {
    return BatchingSettings.newBuilder()
        .setDelayThreshold(batchingSettings.getDelayThreshold())
        .setElementCountThreshold(batchingSettings.getRequestByteThreshold())
        .setElementCountThreshold(Long.valueOf(batchingSettings.getElementCountThreshold()))
        .build();
  }
}
