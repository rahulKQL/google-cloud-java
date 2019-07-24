package com.google.cloud.bigtable.data.v2.it;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.v2.Batcher;
import com.google.api.gax.rpc.ServerStream;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.it.env.TestEnvRule;
import com.google.cloud.bigtable.data.v2.models.BulkMutationBatcher;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowCell;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.cloud.bigtable.data.v2.models.RowMutationEntry;
import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchingIT {

  @ClassRule public static TestEnvRule testEnvRule = new TestEnvRule();

  @Test
  public void test1() throws Exception {
    BigtableDataClient client = testEnvRule.env().getDataClient();
    String tableId = testEnvRule.env().getTableId();
    String family = testEnvRule.env().getFamilyId();
    String rowPrefix = testEnvRule.env().getRowPrefix();
    String myRowKey = "SomeRowKey";
    try (Batcher<RowMutationEntry, Void> bat = client.newBulkMutationBatcher(tableId)) {
      RowMutationEntry entry =
          RowMutationEntry.create(myRowKey)
              .setCell(
                  family,
                  "qualifier1",
                  1_00_000L,
                  "This is my " + "kingdom come this is my kingdom come");

      ApiFuture<Void> future = bat.add(entry);
      ApiFutures.addCallback(
          future,
          new ApiFutureCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
              System.out.println("FAiled");
              throwable.printStackTrace();
              System.out.println("completed---log-fail");
            }

            @Override
            public void onSuccess(Void aVoid) {
              System.out.println("succeeded");
            }
          },
          MoreExecutors.directExecutor());
      future.get();
    }

    Row row = client.readRow(tableId, myRowKey);
    System.out.println("row: " + row.getKey().toStringUtf8());
    for (RowCell cell : row.getCells()) {
      System.out.println("Cell Family: " + cell.getFamily());
      System.out.println("Cell Labels: " + cell.getLabels());
      System.out.println("Cell TimeS : " + cell.getTimestamp());
      System.out.println("Cell Value : " + cell.getValue());
    }
  }

  @Test
  public void test() throws Exception {
    BigtableDataClient client = testEnvRule.env().getDataClient();
    String tableId = testEnvRule.env().getTableId();
    String family = testEnvRule.env().getFamilyId();
    String rowPrefix = testEnvRule.env().getRowPrefix();

    try (Batcher<RowMutationEntry, Void> batcher = client.newBulkMutationBatcher(tableId)) {
      for (int i = 0; i < 10; i++) {
        batcher.add(
            RowMutationEntry.create(rowPrefix + "-" + i)
                .setCell(family, "q-" + i, 10_000, "value-" + i));
      }
    }

    List<Row> expectedRows = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      expectedRows.add(
          Row.create(
              ByteString.copyFromUtf8(rowPrefix + "-" + i),
              Lists.newArrayList(
                  RowCell.create(
                      family,
                      ByteString.copyFromUtf8("q-"+i),
                      10_000,
                      Lists.<String>newArrayList(),
                      ByteString.copyFromUtf8("value-" + i)))));
    }
    ServerStream<Row> actualRows = client.readRows(Query.create(tableId).prefix(rowPrefix));

    Truth.assertThat(actualRows).containsExactlyElementsIn(expectedRows);
  }
}
