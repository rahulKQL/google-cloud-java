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

      for (int i = 0; i < 100; i++) {
        MutateRowsRequest.Entry entry = MutateRowsRequest.Entry.newBuilder()
            .setRowKey(ByteString.copyFromUtf8(rowPrefix + "-" + i)).addMutations(
                Mutation.newBuilder().setSetCell(Mutation.SetCell.newBuilder().setFamilyName(family)
                    .setColumnQualifier(ByteString.copyFromUtf8("test")).setTimestampMicros(1000)
                    .setValue(ByteString.copyFromUtf8("test-values")).build())).build();
        responseList.add(batcher.add(entry));

        // This is an optional
        batcher.flush();
      }

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
