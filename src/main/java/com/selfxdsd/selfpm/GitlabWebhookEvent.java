/**
 * Copyright (c) 2020-2021, Self XDSD Contributors
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

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;

/**
 * Event sent by GitLab via Webhook endpoint.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.5
 * @todo #86:60min Implement and test this class according to the GitLab
 *  Webhook documentation and having GithubWebhookEvent as example.
 */
public final class GitlabWebhookEvent implements Event {

    /**
     * Project where the event happened.
     */
    private final Project project;

    /**
     * Event type.
     */
    private final String type;

    /**
     * Event payload.
     */
    private final JsonObject event;

    /**
     * Ctor.
     * @param project Project where the event happened.
     * @param type Type.
     * @param payload Payload.
     */
    public GitlabWebhookEvent(
        final Project project,
        final String type,
        final String payload
    ) {
        this.project = project;
        this.type = type;
        this.event  = Json.createReader(
            new StringReader(payload)
        ).readObject();
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public Issue issue() {
        return null;
    }

    @Override
    public Comment comment() {
        return null;
    }

    @Override
    public Commit commit() {
        return null;
    }

    @Override
    public Project project() {
        return null;
    }
}
