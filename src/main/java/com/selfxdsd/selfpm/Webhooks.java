/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.selfpm;

import com.selfxdsd.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Webhook endpoints.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.2
 */
@RestController
public final class Webhooks {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        Webhooks.class
    );


    /**
     * Self's core.
     */
    private final Self selfCore;

    /**
     * Ctor.
     * @param selfCore Self Core, injected by Spring automatically.
     */
    @Autowired
    public Webhooks(final Self selfCore) {
        this.selfCore = selfCore;
    }

    /**
     * Webhook for Github projects.
     * @param owner Owner's username (can be a user or organization name).
     * @param name Repo's name.
     * @param type Event type.
     * @param signature Signature sent by Github.
     * @param payload JSON Payload.
     * @return ResponseEntity.
     * @checkstyle ReturnCount (150 lines)
     */
    @PostMapping(
        value = "/github/{owner}/{name}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> github(
        final @PathVariable("owner") String owner,
        final @PathVariable("name") String name,
        final @RequestHeader("X-GitHub-Event") String type,
        final @RequestHeader("X-Hub-Signature") String signature,
        final @RequestBody String payload
    ) {
        final Project project = this.selfCore.projects().getProjectById(
            owner + "/" + name,
            Provider.Names.GITHUB
        );
        if (project != null) {
            LOG.debug("RECEIVED SIGNATURE: " + signature);
            final String calculated = this.hmacHexDigest(
                project.webHookToken(),
                payload
            );
            LOG.debug("CALCULATED SIGNATURE: " + calculated);
            if(calculated != null && calculated.equals(signature)) {
                project.resolve(
                    new Event() {
                        /**
                         * Event payload.
                         */
                        private final JsonObject event = Json.createReader(
                            new StringReader(payload)
                        ).readObject();

                        @Override
                        public String type() {
                            final String resolved;
                            if("issues".equalsIgnoreCase(type)
                                || "pull_request".equalsIgnoreCase(type)) {
                                final String act = event.getString("action");
                                if("opened".equalsIgnoreCase(act)) {
                                    resolved = Type.NEW_ISSUE;
                                } else if ("reopened".equalsIgnoreCase(act)){
                                    resolved = Type.REOPENED_ISSUE;
                                } else {
                                    resolved = type;
                                }
                            } else {
                                resolved = type;
                            }
                            return resolved;
                        }

                        @Override
                        public Issue issue() {
                            return project.projectManager().provider().repo(
                                owner, name
                            ).issues().received(
                                this.event.getJsonObject("issue")
                            );
                        }

                        @Override
                        public Comment comment() {
                            return this.issue().comments().received(
                                this.event.getJsonObject("comment")
                            );
                        }

                        @Override
                        public Commit commit() {
                            return null;
                        }

                        @Override
                        public Project project() {
                            return project;
                        }
                    }
                );
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Calculate the Hmac SHA1 digest.
     * @param key Key.
     * @param body Data to digest.
     * @return Hex HmacSHA1 digest.
     */
    private String hmacHexDigest(final String key, final String body) {
        try {
            final String algorithm = "HmacSHA1";
            final Mac mac = Mac.getInstance(algorithm);
            mac.init(
                new SecretKeySpec(
                    key.getBytes(),
                    algorithm
                )
            );
            final Formatter formatter = new Formatter();
            for (final byte bite : mac.doFinal(body.getBytes())) {
                formatter.format("%02x", bite);
            }
            return "sha1=" + formatter.toString();
        } catch (final NoSuchAlgorithmException | InvalidKeyException ex) {
            return null;
        }
    }
}
