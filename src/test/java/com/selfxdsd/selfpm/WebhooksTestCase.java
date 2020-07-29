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

import com.selfxdsd.api.Project;
import com.selfxdsd.api.Projects;
import com.selfxdsd.api.Provider;
import com.selfxdsd.api.Self;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import javax.json.JsonObject;

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
            hook.github("john", "test", "s3cr3t", "payload")
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
        Mockito.doThrow(IllegalArgumentException.class)
            .when(project)
            .resolve(Mockito.any(JsonObject.class), Mockito.anyString());
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
        Mockito.doNothing()
            .when(project)
            .resolve(Mockito.any(JsonObject.class), Mockito.anyString());
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
                "9317695aaa92d16d7b03dbd25bba34053bd3c3ef",
                "{\"json\":\"payload\"}"
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.OK)
        );
    }
}
