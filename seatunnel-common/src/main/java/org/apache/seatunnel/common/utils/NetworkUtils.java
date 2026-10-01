/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.common.utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class NetworkUtils {

    private boolean checkConnection(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3 * 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkNioConnection(String host, int port) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));

            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);

            if (selector.select(3 * 1000) > 0) {
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable() && channel.finishConnect()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static String[] getAvailableHosts(String[] originHosts) {

        return Arrays.stream(originHosts)
                .filter(
                        hostPort -> {
                            String host = hostPort.split(":")[0];
                            int port = Integer.parseInt(hostPort.split(":")[1]);
                            return checkNioConnection(host, port);
                        })
                .collect(Collectors.toList())
                .toArray(new String[] {});
    }
}
