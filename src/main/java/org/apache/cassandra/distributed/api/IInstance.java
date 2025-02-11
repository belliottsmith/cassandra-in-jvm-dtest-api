/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.distributed.api;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.apache.cassandra.distributed.shared.Metrics;

// The cross-version API requires that an Instance has a constructor signature of (IInstanceConfig, ClassLoader)
public interface IInstance extends IIsolatedExecutor
{
    ICoordinator coordinator();

    IListen listen();

    void schemaChangeInternal(String query);

    default Object[][] executeInternal(String query, Object... args)
    {
        return executeInternalWithResult(query, args).toObjectArrays();
    }

    SimpleQueryResult executeInternalWithResult(String query, Object... args);

    IInstanceConfig config();

    InetSocketAddress broadcastAddress();

    UUID schemaVersion();

    void startup();

    boolean isShutdown();

    Future<Void> shutdown();

    Future<Void> shutdown(boolean graceful);

    int liveMemberCount();

    Metrics metrics();

    NodeToolResult nodetoolResult(boolean withNotifications, String... commandAndArgs);

    default NodeToolResult nodetoolResult(String... commandAndArgs)
    {
        return nodetoolResult(true, commandAndArgs);
    }

    default int nodetool(String... commandAndArgs)
    {
        return nodetoolResult(commandAndArgs).getRc();
    }

    void uncaughtException(Thread t, Throwable e);

    /**
     * Return the number of times the instance tried to call {@link System#exit(int)}.
     * <p>
     * When the instance is shutdown, this state should be saved, but in case not possible should return {@code -1}
     * to indicate "unknown".
     */
    long killAttempts();

    // these methods are not for external use, but for simplicity we leave them public and on the normal IInstance interface
    void startup(ICluster cluster);

    void receiveMessage(IMessage message);

    void receiveMessageWithInvokingThread(IMessage message);

    int getMessagingVersion();

    void setMessagingVersion(InetSocketAddress addressAndPort, int version);

    String getReleaseVersionString();

    void flush(String keyspace);

    void forceCompact(String keyspace, String table);

    default Executor executorFor(int verb) { throw new UnsupportedOperationException(); }

    default boolean getLogsEnabled()
    {
        try
        {
            logs();
            return true;
        }
        catch (UnsupportedOperationException e)
        {
            return false;
        }
    }

    default LogAction logs()
    {
        throw new UnsupportedOperationException();
    }
}
