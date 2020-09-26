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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

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
            hook.github("john", "test", "issues", "90sdwdf8w9", "payload")
                .getStatusCode(),
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
                "{\"json\":\"payload\"}"
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
                "9317695aaa92d16d7b03dbd25bba34053bd3c3ef",
                "{\"json\":\"payload\"}"
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
                "900ac3dbf2d5f8d4923c1d65615289763689ef93",
                "{\"action\":\"opened\"}"
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
                "5ad785edd6a587a36fc5f687eeb6780b6bc1199d",
                "{\"action\":\"reopened\"}"
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
                "e07549de7e41046bba98fdd5cd02b38990b114f3",
                "{\"action\":\"other\"}"
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
                "9a4f065f84a3a28d68aad1b40bd946f79fe6d588",
                "{\"action\":\"edited\"}"
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
}
