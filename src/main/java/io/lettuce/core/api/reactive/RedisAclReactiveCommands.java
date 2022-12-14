/*
 * Copyright 2021-2022 the original author or authors.
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
package io.lettuce.core.api.reactive;

import java.util.List;
import java.util.Map;
import java.util.Set;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.lettuce.core.AclCategory;
import io.lettuce.core.AclSetuserArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.RedisCommand;

/**
 * Reactive executed commands for the ACL-API.
 *
 * @author Mikhael Sokolov
 * @since 6.1
 * @generated by io.lettuce.apigenerator.CreateReactiveApi
 */
public interface RedisAclReactiveCommands<K, V> {

    /**
     * The command shows the available ACL categories if called without arguments.
     *
     * @return AclCategory a list of ACL categories or
     */
    Mono<Set<AclCategory>> aclCat();

    /**
     * The command shows all the Redis commands in the specified category.
     *
     * @param category the specified category
     * @return CommandType a list of commands inside a given category
     */
    Mono<Set<CommandType>> aclCat(AclCategory category);

    /**
     * Delete all the specified ACL users and terminate all the connections that are authenticated with such users.
     *
     * @param usernames the specified usernames
     * @return Long The number of users that were deleted
     */
    Mono<Long> aclDeluser(String... usernames);

    /**
     * Simulate the execution of a given command by a given user.
     *
     * @param username the specified username
     * @param command the specified command
     * @param args the specified args of command
     * @return String reply: OK on success.
     * @since 6.2
     */
    Mono<String> aclDryRun(String username, String command, String... args);

    /**
     * Simulate the execution of a given command by a given user.
     *
     * @param username the specified username
     * @param command the specified command to inspect
     * @return String reply: OK on success.
     * @since 6.2
     */
    Mono<String> aclDryRun(String username, RedisCommand<K, V, ?> command);

    /**
     * The command generates a password.
     *
     * @return String bulk-string-reply 64 bytes string password representing 256 bits of pseudorandom data.
     */
    Mono<String> aclGenpass();

    /**
     * The command generates a password.
     *
     * @param bits amount of bits
     * @return String bulk-string-reply N/4 bytes string password representing N bits of pseudorandom data.
     */
    Mono<String> aclGenpass(int bits);

    /**
     * The command returns all the rules defined for an existing ACL user.
     *
     * @param username the specified username
     * @return Map&lt;String, Object&gt; a map of ACL rule definitions for the user.
     */
    Mono<List<Object>> aclGetuser(String username);

    /**
     * The command shows the currently active ACL rules in the Redis server.
     *
     * @return String a list of strings.
     */
    Flux<String> aclList();

    /**
     * When Redis is configured to use an ACL file (with the aclfile configuration option), this command
     * will reload the ACLs from the file, replacing all the current ACL rules with the ones defined in the file.
     *
     * @return String simple-string-reply OK or error message.
     */
    Mono<String> aclLoad();

    /**
     * The command shows a list of recent ACL security events.
     *
     * @return Map&lt;K,Object&gt; list of security events.
     */
    Flux<Map<String, Object>> aclLog();

    /**
     * The command shows a list of recent ACL security events.
     *
     * @param count max count of events
     * @return Map&lt;K, Object&gt; list of security events.
     */
    Flux<Map<String, Object>> aclLog(int count);

    /**
     * The command clears ACL security events.
     *
     * @return String simple-string-reply OK if the security log was cleared.
     */
    Mono<String> aclLogReset();

    /**
     * When Redis is configured to use an ACL file (with the aclfile configuration option),
     * this command will save the currently defined ACLs from the server memory to the ACL file.
     *
     * @return String simple-string-reply OK or error message.
     */
    Mono<String> aclSave();

    /**
     * Create an ACL user with the specified rules or modify the rules of an existing user.
     *
     * @param username the specified username
     * @param setuserArgs rules
     * @return String simple-string-reply OK or error message.
     */
    Mono<String> aclSetuser(String username, AclSetuserArgs setuserArgs);

    /**
     * The command shows a list of all the usernames of the currently configured users in the Redis ACL system.
     *
     * @return K a list of usernames.
     */
    Flux<String> aclUsers();

    /**
     * The command shows a list of all the usernames of the currently configured users in the Redis ACL system.
     *
     * @return K bulk-string-reply the username of the current connection.
     */
    Mono<String> aclWhoami();
}