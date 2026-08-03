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

import org.apache.seatunnel.shade.org.apache.commons.lang3.StringUtils;

import lombok.Data;

import java.io.Serializable;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.seatunnel.common.utils.NetworkUtils.getAvailableHosts;

public final class JdbcUrlUtil {
    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "^(?<url>jdbc:.+?//"
                            + "(?<hosts>(?:[^,]+:\\d+)(?:,[^,]+:\\d+)*))"
                            + "(/(?<database>.*?))*"
                            + "(?<suffix>\\?.*)*$");

    private JdbcUrlUtil() {}

    public static JdbcUrlUtil.UrlInfo getUrlInfo(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if (matcher.find()) {
            String urlWithoutDatabase = matcher.group("url");
            String[] hosts= matcher.group("hosts").split(",");
            String database = matcher.group("database");
            return new JdbcUrlUtil.UrlInfo(
                    url,
                    urlWithoutDatabase,
                    hosts,
                    database,
                    matcher.group("suffix"));
        }
        throw new IllegalArgumentException("The jdbc url format is incorrect: " + url);
    }

    @Data
    public static class UrlInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String origin;
        private final String urlWithoutDatabase;
        private final String[] hosts;
        private final String host;
        private final Integer port;
        private final String suffix;
        private final String defaultDatabase;

        public UrlInfo(
                String origin,
                String urlWithoutDatabase,
                String[] hosts,
                String defaultDatabase,
                String suffix) {
            this.origin = origin;
            this.urlWithoutDatabase = urlWithoutDatabase;
            this.hosts = hosts;
            String[] availableHosts=getAvailableHosts(hosts);
            String randomHost=getRandomHost(availableHosts);
            this.host=randomHost.split(":")[0];
            this.port = Integer.valueOf(randomHost.split(":")[1]);
            this.defaultDatabase = defaultDatabase;
            this.suffix = suffix == null ? "" : suffix;
        }

        public Optional<String> getUrlWithDatabase() {
            return StringUtils.isBlank(defaultDatabase)
                    ? Optional.empty()
                    : Optional.of(urlWithoutDatabase + "/" + defaultDatabase + suffix);
        }

        public Optional<String> getDefaultDatabase() {
            return StringUtils.isBlank(defaultDatabase)
                    ? Optional.empty()
                    : Optional.of(defaultDatabase);
        }

        public String getUrlWithDatabase(String database) {
            return urlWithoutDatabase + "/" + database + suffix;
        }

        public String getRandomHost(String[] originHosts){
            int randomIndex= new Random().nextInt(originHosts.length);
            return originHosts[randomIndex];
        }
    }
}
