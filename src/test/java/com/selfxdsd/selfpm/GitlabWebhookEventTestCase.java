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

import com.selfxdsd.api.Event;
import com.selfxdsd.api.Project;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import javax.json.Json;

/**
 * Unit tests for {@link GitlabWebhookEvent}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.5
 */
public final class GitlabWebhookEventTestCase {

    /**
     * It can return the NEW_ISSUE type for a newly opened Issue.
     */
    @Test
    public void typeNewIssueForOpenedIssue() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Issue Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("state", "opened")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo(Event.Type.NEW_ISSUE)
        );
    }

    /**
     * It can return the NEW_ISSUE type for a newly opened Merge Request.
     */
    @Test
    public void typeNewIssueForOpenedMergeRequest() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Merge Request Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("state", "opened")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo(Event.Type.NEW_ISSUE)
        );
    }

    /**
     * It can return the REOPENED_ISSUE type for a newly opened Issue.
     */
    @Test
    public void typeReopenedIssueForReopenedIssue() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Issue Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("state", "reopened")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo(Event.Type.REOPENED_ISSUE)
        );
    }

    /**
     * It can return the REOPENED_ISSUE type for a newly opened Merge Request.
     */
    @Test
    public void typeReopenedIssueForReopenedMergeRequest() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Merge Request Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("state", "reopened")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo(Event.Type.REOPENED_ISSUE)
        );
    }

    /**
     * It returns the original type ("Issue Hook") if the Issue's
     * state is not opened or reopened.
     */
    @Test
    public void originalTypeForOtherIssueState() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Issue Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("state", "closed")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo("Issue Hook")
        );
    }

    /**
     * It returns the original type ("Merge Request Hook") if the MR's
     * state is not opened or reopened.
     */
    @Test
    public void originalTypeForOtherMergeRequestState() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Merge Request Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("state", "closed")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo("Merge Request Hook")
        );
    }

    /**
     * It returns the original type if the hook type is not Issue/MR Hook or
     * Note Hook (Comment).
     */
    @Test
    public void originalTypeOnNotIssueAndNotComment() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Commit Hook",
            "{}"
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo("Commit Hook")
        );
    }

    /**
     * It can return the ISSUE_COMMENT type for a comment on Issue.
     */
    @Test
    public void typeIssueCommentForIssueComment() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Note Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("noteable_type", "Issue")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo(Event.Type.ISSUE_COMMENT)
        );
    }

    /**
     * It can return the ISSUE_COMMENT type for a comment on MR.
     */
    @Test
    public void typeIssueCommentForMergeRequestComment() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Note Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("noteable_type", "MergeRequest")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo(Event.Type.ISSUE_COMMENT)
        );
    }

    /**
     * It can return the original type for comments
     * on other entities than Issue or MR.
     */
    @Test
    public void originalTypeForOtherComment() {
        final Event gitlabEvent = new GitlabWebhookEvent(
            Mockito.mock(Project.class),
            "Note Hook",
            Json.createObjectBuilder()
                .add(
                    "object_attributes",
                    Json.createObjectBuilder()
                        .add("noteable_type", "Commit")
                ).build().toString()
        );
        MatcherAssert.assertThat(
            gitlabEvent.type(),
            Matchers.equalTo("Note Hook")
        );
    }


}
