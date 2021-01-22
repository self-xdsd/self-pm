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
 * @see <a href="https://docs.gitlab.com/ee/user/project/integrations/webhooks.html">Documentation.</a>
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
        final String resolved;
        if("Issue Hook".equalsIgnoreCase(this.type)
            || "Merge Request Hook".equalsIgnoreCase(this.type)) {
            final String state = this.event.getJsonObject(
                "object_attributes"
            ).getString("state", "");
            if(state.startsWith("open")) {
                resolved = Type.NEW_ISSUE;
            } else if (state.startsWith("reopen")) {
                resolved = Type.REOPENED_ISSUE;
            } else {
                resolved = this.type;
            }
        } else if("Note Hook".equalsIgnoreCase(this.type)) {
            final String noteableType = this.event.getJsonObject(
                "object_attributes"
            ).getString("noteable_type", "");
            if("Issue".equalsIgnoreCase(noteableType)
                || "MergeRequest".equalsIgnoreCase(noteableType)) {
                resolved = Type.ISSUE_COMMENT;
            } else {
                resolved = this.type;
            }
        } else {
            resolved = this.type;
        }
        return resolved;
    }

    @Override
    public Issue issue() {
        final String iid;
        if("Issue Hook".equalsIgnoreCase(this.type)
            || "Merge Request Hook".equalsIgnoreCase(this.type)) {
            iid = this.event.getJsonObject("object_attributes")
                .getString("iid");
        } else if("Note Hook".equalsIgnoreCase(this.type)) {
            final String noteableType = this.event.getJsonObject(
                "object_attributes"
            ).getString("noteable_type", "");
            if("Issue".equalsIgnoreCase(noteableType)) {
                iid = this.event.getJsonObject("issue").getString("iid");
            } else if("MergeRequest".equalsIgnoreCase(noteableType)){
                iid = this.event.getJsonObject("merge_request")
                    .getString("iid");
            } else {
                iid = null;
            }
        } else {
            iid = null;
        }
        final Issue issue;
        if(iid != null) {
            final String repoFullName = this.project.repoFullName();
            issue = this.project.projectManager().provider().repo(
                repoFullName.split("/")[0],
                repoFullName.split("/")[1]
            ).issues().getById(iid);
        } else {
            issue = null;
        }
        return issue;
    }

    @Override
    public Comment comment() {
        final JsonObject jsonComment;
        if("Note Hook".equalsIgnoreCase(this.type)) {
            final JsonObject attributes = this.event.getJsonObject(
                "object_attributes"
            );
            final String noteableType = attributes.getString(
                "noteable_type", ""
            );
            if ("Issue".equalsIgnoreCase(noteableType)
                || "MergeRequest".equalsIgnoreCase(noteableType)) {
                jsonComment = Json.createObjectBuilder()
                    .add("id", attributes.getString("id"))
                    .add("body", attributes.getString("note"))
                    .add(
                        "author",
                        Json.createObjectBuilder()
                            .add(
                                "username",
                                this.event.getJsonObject("user")
                                    .getString("username")
                            )
                    ).build();
            } else {
                jsonComment = null;
            }
        } else {
            jsonComment = null;
        }
        final Comment comment;
        if(jsonComment != null) {
            comment = this.issue().comments().received(jsonComment);
        } else {
            comment = null;
        }
        return comment;
    }

    @Override
    public Commit commit() {
        return null;
    }

    @Override
    public Project project() {
        return this.project;
    }
}
