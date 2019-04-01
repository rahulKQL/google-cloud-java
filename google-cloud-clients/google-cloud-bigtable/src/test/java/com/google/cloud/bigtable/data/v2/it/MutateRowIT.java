/*
 * Copyright 2018 Google LLC
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

import static com.google.common.truth.Truth.assertThat;

import com.google.api.core.ApiFuture;
import com.google.cloud.bigtable.data.v2.it.env.TestEnvRule;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.protobuf.ByteString;
import java.util.concurrent.TimeUnit;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MutateRowIT {
  @ClassRule public static TestEnvRule testEnvRule = new TestEnvRule();

  @Test
  public void test() throws Exception {
    String rowKey = testEnvRule.env().getRowPrefix() + "testA";
    String familyId = testEnvRule.env().getFamilyId();

    testEnvRule
        .env()
        .getDataClient()
        .mutateRowAsync(
            RowMutation.create(testEnvRule.env().getTableId(), rowKey)
                .setCell(familyId, "q", "myVal")
                .setCell(familyId, "q2", "myVal2")
                .setCell(familyId, "q3", "myVal3"))
        .get(1, TimeUnit.MINUTES);

    ApiFuture<Void> future = testEnvRule
        .env().getDataClient().mutateRowAsync(
       RowMutation.create(testEnvRule.env().getTableId(), rowKey)
                    .setCell(familyId, "q", "SomeOtherVal")
                    .setCell(familyId, "q2", "SomeOtherVal2")
                    .setCell(familyId, "q3", "SomeOtherVal3"));

    future.cancel(true);

    testEnvRule
        .env()
        .getDataClient()
        .mutateRowAsync(
            RowMutation.create(testEnvRule.env().getTableId(), rowKey).deleteCells(familyId, "q2"))
        .get(1, TimeUnit.MINUTES);

    Row row =
        testEnvRule
            .env()
            .getDataClient()
            .readRowsCallable()
            .first()
            .call(Query.create(testEnvRule.env().getTableId()).rowKey(rowKey));

    assertThat(row.getCells()).hasSize(2);
    assertThat(row.getCells().get(0).getValue()).isEqualTo(ByteString.copyFromUtf8("myVal"));
    assertThat(row.getCells().get(1).getValue()).isEqualTo(ByteString.copyFromUtf8("myVal3"));
  }
}
