## Batching-Api Sample App

 #### BigtableDataClient 
 
 ```java
 public class BigtableDataClient implements AutoCloseable {
   /**
    * Mutates entries of a row in batch.
    *
    * <p>Sample code:
    *
    * <pre>{@code
    * try (BigtableDataClient bigtableDataClient = BigtableDataClient.create("[PROJECT]", "[INSTANCE]")) {
    *   try (Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> batcher =
    *     bigtableDataClient.createMutateRowsRequestBatcher("[TABLE]")) {
    *     for (String someValue : someCollection) {
    *      MutateRowsRequest.Entry entry =
    *             MutateRowsRequest.Entry.newBuilder()
    *                 .setRowKey("[ROW KEY]")
    *                 .addMutations(
    *                     Mutation.newBuilder()
    *                         .setSetCell(
    *                             Mutation.SetCell.newBuilder()
    *                                 .setFamilyName("[FAMILY NAME]")
    *                                 .setColumnQualifier("[QUALIFIER]")
    *                                 .setTimestampMicros("[TIMESTAMP]")
    *                                 .setValue("[VALUE]")
    *                                 .build()))
    *                 .build();
    *
    *       ApiFuture<MutateRowsResponse.Entry> entryFuture = batcher.add(mutation);
    *     }
    *   }
    *   // After `batcher` is closed, all mutations have been applied
    * }
    * }</pre>
    */
   public Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> createMutateRowsRequestBatcher(
       final String tableId) {
     return createMutateRowsRequestBatcher(tableId, null);
   }
   
   /**
    * Mutates entries of a row in batch.
    *
    * <p>Sample code:
    *
    * <pre>{@code
    * try (BigtableDataClient bigtableDataClient = BigtableDataClient.create("[PROJECT]", "[INSTANCE]")) {
    *   try (Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> batcher =
    *     bigtableDataClient.createMutateRowsRequestBatcher("[TABLE]", "[BATCH-SETTINGS]")) {
    *     for (String someValue : someCollection) {
    *      MutateRowsRequest.Entry entry =
    *             MutateRowsRequest.Entry.newBuilder()
    *                 .setRowKey("[ROW KEY]")
    *                 .addMutations(
    *                     Mutation.newBuilder()
    *                         .setSetCell(
    *                             Mutation.SetCell.newBuilder()
    *                                 .setFamilyName("[FAMILY NAME]")
    *                                 .setColumnQualifier("[QUALIFIER]")
    *                                 .setTimestampMicros("[TIMESTAMP]")
    *                                 .setValue("[VALUE]")
    *                                 .build()))
    *                 .build();
    *
    *       ApiFuture<MutateRowsResponse.Entry> entryFuture = batcher.add(mutation);
    *     }
    *   }
    *   // After `batcher` is closed, all mutations have been applied
    * }
    * }</pre>
    */
   public Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> createMutateRowsRequestBatcher(
       final String tableId, BatchingSettings batchingSettings) {
     return stub.createMutateRowsRequestBatcher(tableId, batchingSettings);
   } 
}  
```

#### EnhancedBigtableStub.java

```java
public class EnhancedBigtableStub {
  private final BatchingSettings baseBatchingSettings;
  
  /**
   * This operation creates an {@link com.google.api.gax.batching.v2.EntryBatcherFactory} which accepts
   * {@link com.google.api.gax.rpc.ServerStreamingCallable} as an argument.
   *
   * If {@link com.google.api.gax.batching.BatchingSettings} not present in the received argument, 
   * then it uses a default baseBatchingsettings.
   */
  public Batcher<MutateRowsRequest.Entry, MutateRowsResponse.Entry> createMutateRowsRequestBatcher(
      String tableId, BatchingSettings batchingSettings) {
    Preconditions.checkNotNull(tableId, "tableId can't be null");
    Preconditions.checkArgument(!tableId.isEmpty(), "tableId can't be empty");

    final String tableName = NameUtil.formatTableName(settings.getProjectId(),
        settings.getInstanceId(), tableId);

    if(batchingSettings == null) {
      batchingSettings = baseBatchingSettings;
    }

    BatchingDescriptor<MutateRowsRequest.Entry, MutateRowsResponse.Entry, MutateRowsRequest,
        MutateRowsResponse> batchingDescriptor = new MutateRowsEntryBatchingDescriptor(tableName,
        settings.getAppProfileId());

    return new EntryBatcherFactory<>(
        batchingDescriptor,
        settings.getExecutorProvider().getExecutor(),
        batchingSettings
    ).createStreamBatcher(stub.mutateRowsCallable());
  }

  /**
   *  We can create {@link EntryBatcherFactory} with a {@link SpoolingCallable} like this:
   *
   BatchingDescriptor<MutateRowsRequest.Entry, MutateRowsResponse.Entry, MutateRowsRequest,
   List<MutateRowsResponse>> spoolingBatchingDescriptor =
   new MutateRowsUnaryBatchingDescriptor(tableName, settings.getAppProfileId());

   return new EntryBatcherFactory<>(
       spoolingBatchingDescriptor,
       settings.getExecutorProvider().getExecutor(),
       batchingSettings
       ).createUnaryBatcher(stub.mutateRowsCallable().all());
   */

}
```

#### An implementation of BatchingDescriptor.java

```java
public class MutateRowsEntryBatchingDescriptor implements
    BatchingDescriptor<MutateRowsRequest.Entry, MutateRowsResponse.Entry, MutateRowsRequest, MutateRowsResponse> {
  private final String tableName;
  private final String appProfileId;

  public MutateRowsEntryBatchingDescriptor(String tableName, String appProfileId) {
    this.tableName = tableName;
    this.appProfileId = appProfileId;
  }

  @Override
  public RequestBuilder<MutateRowsRequest.Entry, MutateRowsRequest> newRequestBuilder() {
    return new RequestBuilder<MutateRowsRequest.Entry, MutateRowsRequest>() {

      private final MutateRowsRequest.Builder builder =
          MutateRowsRequest.newBuilder().setTableName(tableName).setAppProfileId(appProfileId);

      @Override
      public void add(MutateRowsRequest.Entry entry) {
        builder.addEntries(entry);
      }

      @Override
      public MutateRowsRequest build() {
        return builder.build();
      }
    };
  }

  @Override
  public void splitResponse(MutateRowsResponse mutateRowsResponse,
      List<SettableApiFuture<MutateRowsResponse.Entry>> list) {
    for (int i = 0; i < list.size() && i < mutateRowsResponse.getEntriesCount(); i++) {
      setResult(mutateRowsResponse.getEntries(i), list.get(i));
    }
  }

  @Override
  public void setResult(MutateRowsResponse.Entry entry,
      SettableApiFuture<MutateRowsResponse.Entry> settableApiFuture) {
    settableApiFuture.set(entry);
  }

  @Override
  public void splitException(Throwable throwable,
      List<SettableApiFuture<MutateRowsResponse.Entry>> list) {
    for (SettableApiFuture<MutateRowsResponse.Entry> entryFuture : list) {
      entryFuture.setException(throwable);
    }
  }

  @Override
  public long countBytes(MutateRowsRequest.Entry entry) {
    if (entry == null) {
      return 0;
    }
    return entry.getSerializedSize();
  }
}
```

#### MutateRowsEntryBatcherIT.java

```java
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
      List<MutateRowsResponse.Entry> values = ApiFutures.allAsList(responseList).get();

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
```