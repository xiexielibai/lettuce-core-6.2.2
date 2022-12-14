/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lettuce.core.masterreplica;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ConnectionIntent;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.resource.ClientResources;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MasterReplicaChannelWriterUnitTests {

    @Mock
    private MasterReplicaConnectionProvider<String, String> connectionProvider;

    @Mock
    private ClientResources clientResources;

    @Mock
    private StatefulRedisConnection<String, String> connection;

    @Test
    void shouldReturnIntentForWriteCommand() {

        RedisCommand<String, String, String> set = new Command<>(CommandType.SET, null);
        RedisCommand<String, String, String> mset = new Command<>(CommandType.MSET, null);

        assertThat(MasterReplicaChannelWriter.getIntent(Arrays.asList(set, mset)))
                .isEqualTo(ConnectionIntent.WRITE);

        assertThat(MasterReplicaChannelWriter.getIntent(Collections.singletonList(set)))
                .isEqualTo(ConnectionIntent.WRITE);
    }

    @Test
    void shouldReturnDefaultIntentForNoCommands() {

        assertThat(MasterReplicaChannelWriter.getIntent(Collections.emptyList()))
                .isEqualTo(ConnectionIntent.WRITE);
    }

    @Test
    void shouldReturnIntentForReadCommand() {

        RedisCommand<String, String, String> get = new Command<>(CommandType.GET, null);
        RedisCommand<String, String, String> mget = new Command<>(CommandType.MGET, null);

        assertThat(MasterReplicaChannelWriter.getIntent(Arrays.asList(get, mget)))
                .isEqualTo(ConnectionIntent.READ);

        assertThat(MasterReplicaChannelWriter.getIntent(Collections.singletonList(get)))
                .isEqualTo(ConnectionIntent.READ);
    }

    @Test
    void shouldReturnIntentForMixedCommands() {

        RedisCommand<String, String, String> set = new Command<>(CommandType.SET, null);
        RedisCommand<String, String, String> mget = new Command<>(CommandType.MGET, null);

        assertThat(MasterReplicaChannelWriter.getIntent(Arrays.asList(set, mget)))
                .isEqualTo(ConnectionIntent.WRITE);

        assertThat(MasterReplicaChannelWriter.getIntent(Collections.singletonList(set)))
                .isEqualTo(ConnectionIntent.WRITE);
    }

    @Test
    void shouldBindTransactionsToMaster() {

        MasterReplicaChannelWriter writer = new MasterReplicaChannelWriter(connectionProvider, clientResources);

        when(connectionProvider.getConnectionAsync(any(ConnectionIntent.class)))
                .thenReturn(CompletableFuture.completedFuture(connection));

        writer.write(mockCommand(CommandType.MULTI));
        writer.write(mockCommand(CommandType.GET));
        writer.write(mockCommand(CommandType.EXEC));

        verify(connectionProvider, times(3)).getConnectionAsync(ConnectionIntent.WRITE);
    }

    @Test
    void shouldBindTransactionsToMasterInBatch() {

        MasterReplicaChannelWriter writer = new MasterReplicaChannelWriter(connectionProvider, clientResources);

        when(connectionProvider.getConnectionAsync(any(ConnectionIntent.class)))
                .thenReturn(CompletableFuture.completedFuture(connection));

        List<Command<String, String, String>> commands = Arrays.asList(mockCommand(CommandType.MULTI),
                mockCommand(CommandType.GET), mockCommand(CommandType.EXEC));

        writer.write(commands);

        verify(connectionProvider).getConnectionAsync(ConnectionIntent.WRITE);
    }

    @Test
    void shouldDeriveIntentFromCommandTypeAfterTransaction() {

        MasterReplicaChannelWriter writer = new MasterReplicaChannelWriter(connectionProvider, clientResources);

        when(connectionProvider.getConnectionAsync(any(ConnectionIntent.class)))
                .thenReturn(CompletableFuture.completedFuture(connection));

        writer.write(mockCommand(CommandType.MULTI));
        writer.write(mockCommand(CommandType.EXEC));
        writer.write(mockCommand(CommandType.GET));

        verify(connectionProvider, times(2)).getConnectionAsync(ConnectionIntent.WRITE);
        verify(connectionProvider).getConnectionAsync(ConnectionIntent.READ);
    }

    @Test
    void shouldDeriveIntentFromCommandTypeAfterDiscardedTransaction() {

        MasterReplicaChannelWriter writer = new MasterReplicaChannelWriter(connectionProvider, clientResources);

        when(connectionProvider.getConnectionAsync(any(ConnectionIntent.class)))
                .thenReturn(CompletableFuture.completedFuture(connection));

        writer.write(mockCommand(CommandType.MULTI));
        writer.write(mockCommand(CommandType.DISCARD));
        writer.write(mockCommand(CommandType.GET));

        verify(connectionProvider, times(2)).getConnectionAsync(ConnectionIntent.WRITE);
        verify(connectionProvider).getConnectionAsync(ConnectionIntent.READ);
    }

    @Test
    void shouldDeriveIntentFromCommandBatchTypeAfterDiscardedTransaction() {

        MasterReplicaChannelWriter writer = new MasterReplicaChannelWriter(connectionProvider, clientResources);

        when(connectionProvider.getConnectionAsync(any(ConnectionIntent.class)))
                .thenReturn(CompletableFuture.completedFuture(connection));

        List<Command<String, String, String>> commands = Arrays.asList(mockCommand(CommandType.MULTI),
                mockCommand(CommandType.EXEC));

        writer.write(commands);
        writer.write(Collections.singletonList(mockCommand(CommandType.GET)));

        verify(connectionProvider).getConnectionAsync(ConnectionIntent.WRITE);
        verify(connectionProvider).getConnectionAsync(ConnectionIntent.READ);
    }

    private static Command<String, String, String> mockCommand(CommandType multi) {
        return new Command<>(multi, new StatusOutput<>(StringCodec.UTF8));
    }
}
