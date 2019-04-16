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
package com.google.cloud.bigtable.data.v2.it;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.v2.Batcher;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.bigtable.v2.MutateRowsResponse;
import com.google.bigtable.v2.Mutation;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.internal.NameUtil;
import com.google.cloud.bigtable.data.v2.it.env.TestEnvRule;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MutateRowsEntryBatcherIT {

  private static final Logger LOGGER = Logger.getLogger(MutateRowsEntryBatcherIT.class.getName());

  @ClassRule
  public static TestEnvRule testEnvRule = new TestEnvRule();

  @Test
  public void testBatcherApi() throws Exception {
    BigtableDataClient client = testEnvRule.env().getDataClient();
    String family = testEnvRule.env().getFamilyId();
    String rowPrefix = testEnvRule.env().getRowPrefix();
    long startTime = System.nanoTime();
    String table_name = NameUtil
        .formatTableName(testEnvRule.env().getProjectId(), testEnvRule.env().getInstanceId(),
            testEnvRule.env().getTableId());

    List<ApiFuture<MutateRowsResponse.Entry>> responseList = new ArrayList<>();

    // Batcher would be created with a context passed to surface API's endpoint
    try (Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> batcher = client
        .createMutateRowsRequestBatcher(table_name)) {

      for (int i = 0; i < 1000; i++) {
        MutateRowsRequest.Entry entry = MutateRowsRequest.Entry.newBuilder()
            .setRowKey(ByteString.copyFromUtf8(rowPrefix + "-" + i)).addMutations(
                Mutation.newBuilder().setSetCell(Mutation.SetCell.newBuilder().setFamilyName(family)
                    .setColumnQualifier(ByteString.copyFromUtf8("test")).setTimestampMicros(1000)
                    .setValue(ByteString.copyFromUtf8("test-values")).build())).build();
        responseList.add(batcher.add(entry));
      }
      batcher.flush();
      // flush() is not needed as AutoCloseable has already flushed.
      List<MutateRowsResponse.Entry> values = ApiFutures.allAsList(responseList)
          .get(1, TimeUnit.MINUTES);

      int i = 0;
      for(MutateRowsResponse.Entry res : values){
        if(i%50 == 0){
          System.out.printf("Entries Index: %s, Status: %s  \n", res.getIndex(), res.getStatus());
        }
        i++;
      }
      System.out.println("Total element: " +  i);
      long endTime = System.nanoTime();
      long durationInNano = (endTime - startTime);
      LOGGER.info("Finished Execution in Ms: "+ TimeUnit.NANOSECONDS.toMillis(durationInNano));
    }
  }
}
