package com.google.cloud.bigtable.data.v2.it;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.v2.Batcher;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.bigtable.v2.MutateRowsResponse;
import com.google.bigtable.v2.Mutation;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.it.env.TestEnvRule;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MutateRowsEntryBatcherIT {

  @ClassRule
  public static TestEnvRule testEnvRule = new TestEnvRule();

  @Test
  public void testBatcherApi() throws Exception {
    BigtableDataClient client = testEnvRule.env().getDataClient();
    String tableId = testEnvRule.env().getTableId();
    String family = testEnvRule.env().getFamilyId();
    String rowPrefix = testEnvRule.env().getRowPrefix();

    try (Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> batcher =
        client.createMutateRowsRequestBatcher(tableId)) {
      List<ApiFuture<MutateRowsResponse.Entry>> responseList = new ArrayList<>();
      for (int i = 0; i < 1_000; i++) {
        MutateRowsRequest.Entry entry =
            MutateRowsRequest.Entry.newBuilder()
                .setRowKey(ByteString.copyFromUtf8(rowPrefix + "-" + i))
                .addMutations(
                    Mutation.newBuilder()
                        .setSetCell(
                            Mutation.SetCell.newBuilder()
                                .setFamilyName(family)
                                .setColumnQualifier(ByteString.copyFromUtf8("test"))
                                .setTimestampMicros(1000)
                                .setValue(ByteString.copyFromUtf8("test-values"))
                                .build()))
                .build();
        responseList.add(batcher.add(entry));
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
    }
  }
}
