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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import javax.json.Json;

/**
 * Unit tests for {@link Webhooks}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.2
 */
public final class WebhooksTestCase {

    /**
     * Webhooks.github returns No Content if the project is
     * not found.
     */
    @Test
    public void githubProjectNotFound() {
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(null);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issues",
                "90sdwdf8w9",
                Json.createObjectBuilder()
                    .add("action", "open")
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build().toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.NO_CONTENT)
        );
    }

    /**
     * If the project signature does not match,
     * the hook should return 400 BAD REQUEST.
     */
    @Test
    public void githubProjectBadSignature() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("project_wh_token");
        Mockito.doThrow(IllegalStateException.class)
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issues",
                "bad5aaa92d16d7b03dbd25bba34053bd3c3ef",
                "{\"repository\":{\"full_name\":\"john/test\"}}"
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.BAD_REQUEST)
        );
    }

    /**
     * A Github project resolves ok.
     */
    @Test
    public void githubProjectResolvesOk() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("project_wh_token");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        Mockito.doNothing()
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issues",
                "sha1=853a76e61f8c83758a25641a7018ef6ba1a757e8",
                "{\"repository\":{\"full_name\":\"john/test\"}}"
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );
    }

    /**
     * A Github project resolves a "newIssue" event.
     */
    @Test
    public void githubProjectResolvesNewIssue() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("project_wh_token");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        Mockito.doNothing()
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issues",
                "sha1=e0649975c486f8f5ba5f16c9d9b1a811bea7120d",
                Json.createObjectBuilder()
                    .add("action", "opened")
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build()
                    .toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );

        final ArgumentCaptor<Event> event = ArgumentCaptor.forClass(
            Event.class
        );
        Mockito.verify(project, Mockito.times(1)).resolve(event.capture());
        MatcherAssert.assertThat(
            event.getValue().type(),
            Matchers.equalTo("newIssue")
        );
    }

    /**
     * A Github project resolves a "reopened" issue event.
     */
    @Test
    public void githubProjectResolvesReopenedIssue() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("project_wh_token");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        Mockito.doNothing()
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issues",
                "sha1=a35fac5121c90f48672ed2701746631877964f62",
                Json.createObjectBuilder()
                    .add("action", "reopened")
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build()
                    .toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );

        final ArgumentCaptor<Event> event = ArgumentCaptor.forClass(
            Event.class
        );
        Mockito.verify(project, Mockito.times(1)).resolve(event.capture());
        MatcherAssert.assertThat(
            event.getValue().type(),
            Matchers.equalTo("reopened")
        );
    }

    /**
     * A Github project resolves a "reopened" event.
     */
    @Test
    public void githubProjectResolvesOtherIssuesEvent() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("project_wh_token");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        Mockito.doNothing()
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issues",
                "sha1=69cff7d288f62021738c8b0e75ffdac025a13ab6",
                Json.createObjectBuilder()
                    .add("action", "other")
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build()
                    .toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );

        final ArgumentCaptor<Event> event = ArgumentCaptor.forClass(
            Event.class
        );
        Mockito.verify(project, Mockito.times(1)).resolve(event.capture());
        MatcherAssert.assertThat(
            event.getValue().type(),
            Matchers.equalTo("issues")
        );
    }

    /**
     * A Github project resolves an "issue_comment" event.
     */
    @Test
    public void githubProjectResolvesIssueCommentEvent() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("project_wh_token");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        Mockito.doNothing()
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITHUB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.github(
                "john",
                "test",
                "issue_comment",
                "sha1=9d2e4ddc3d7c8f1709862427f5089889e2b09f07",
                Json.createObjectBuilder()
                    .add("action", "edited")
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build()
                    .toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );

        final ArgumentCaptor<Event> event = ArgumentCaptor.forClass(
            Event.class
        );
        Mockito.verify(project, Mockito.times(1)).resolve(event.capture());
        MatcherAssert.assertThat(
            event.getValue().type(),
            Matchers.equalTo("issue_comment")
        );
    }

    /**
     * Webhooks.gitlab returns No Content if the project is
     * not found.
     */
    @Test
    public void gitlabProjectNotFound() {
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITLAB)
        ).thenReturn(null);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.gitlab(
                "john",
                "test",
                "Push Hook",
                "90sdwdf8w9",
                Json.createObjectBuilder()
                    .add("action", "opened")
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build()
                    .toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.NO_CONTENT)
        );
    }

    /**
     * If the project token does not match, so
     * the GitLab hook should return 400 BAD REQUEST.
     */
    @Test
    public void gitlabProjectBadToken() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("token123");
        Mockito.doThrow(IllegalStateException.class)
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITLAB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);
        final Webhooks hook = new Webhooks(self);
        MatcherAssert.assertThat(
            hook.gitlab(
                "john",
                "test",
                "Push Hook",
                "token123456789",
                Json.createObjectBuilder()
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "john/test")
                    ).build()
                    .toString()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.BAD_REQUEST)
        );
    }

    /**
     * If the event is "Push Hook", the payload should be forwarded to
     * SelfTodos.
     */
    @Test
    public void gitlabWebhookForwardsPushToSelfTodos() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("token123");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITLAB);
        Mockito.doThrow(IllegalStateException.class)
            .when(project)
            .resolve(Mockito.any(Event.class));
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITLAB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);

        final SelfTodos selfTodos = Mockito.mock(SelfTodos.class);

        final Webhooks hook = new Webhooks(self, selfTodos);
        MatcherAssert.assertThat(
            hook.gitlab(
                "john",
                "test",
                "Push Hook",
                "token123",
                "{\"json\":\"payload\"}"
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );
        Mockito.verify(
            selfTodos,
            Mockito.times(1)
        ).post(project, "{\"json\":\"payload\"}");
    }

    /**
     * If the event is other than "Push Hook", the Project should resolve
     * the event.
     */
    @Test
    public void gitlabWebhookResolvesEvent() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.webHookToken()).thenReturn("token123");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITLAB);
        final Projects all = Mockito.mock(Projects.class);
        Mockito.when(
            all.getProjectById("john/test", Provider.Names.GITLAB)
        ).thenReturn(project);
        final Self self = Mockito.mock(Self.class);
        Mockito.when(self.projects()).thenReturn(all);

        final SelfTodos selfTodos = Mockito.mock(SelfTodos.class);
        Mockito
            .doThrow(new IllegalStateException("Should not be called"))
            .when(selfTodos).post(project, "{\"json\":\"payload\"}");

        final Webhooks hook = new Webhooks(self, selfTodos);
        MatcherAssert.assertThat(
            hook.gitlab(
                "john",
                "test",
                "Issue Comment",
                "token123",
                "{\"repository\":{\"full_name\":\"john/test\"}}"
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );
        Mockito.verify(
            project,
            Mockito.times(1)
        ).resolve(Mockito.any(Event.class));
    }
}
