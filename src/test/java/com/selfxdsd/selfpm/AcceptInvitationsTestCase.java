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
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link AcceptInvitations}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.4
 */
public final class AcceptInvitationsTestCase {

    /**
     * It should work fine when there are no invitations.
     */
    @Test
    public void acceptsZeroInvitations() {
        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        final Invitations invitations = Mockito.mock(Invitations.class);
        Mockito.when(invitations.iterator()).thenReturn(
            new ArrayList<Invitation>().iterator()
        );
        final Provider provider = Mockito.mock(Provider.class);
        Mockito.when(provider.invitations()).thenReturn(invitations);
        Mockito.when(manager.provider()).thenReturn(provider);

        final ProjectManagers all = Mockito.mock(ProjectManagers.class);
        Mockito.when(all.iterator()).thenReturn(
            Arrays.asList(manager).iterator()
        );
        final Self core = Mockito.mock(Self.class);
        Mockito.when(core.projectManagers()).thenReturn(all);

        final AcceptInvitations component = new AcceptInvitations(core);
        component.acceptInvitations();
    }

    /**
     * It should accept all found invitations.
     */
    @Test
    public void acceptsInvitations() {
        final List<Invitation> mocks = new ArrayList<>();
        for(int idx = 0; idx <3; idx++){
            final Invitation invitation = Mockito.mock(Invitation.class);
            Mockito.doNothing().when(invitation).accept();
            mocks.add(invitation);
        }

        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        final Invitations invitations = Mockito.mock(Invitations.class);
        Mockito.when(invitations.iterator()).thenReturn(
            mocks.iterator()
        );
        final Provider provider = Mockito.mock(Provider.class);
        Mockito.when(provider.invitations()).thenReturn(invitations);
        Mockito.when(manager.provider()).thenReturn(provider);

        final ProjectManagers all = Mockito.mock(ProjectManagers.class);
        Mockito.when(all.iterator()).thenReturn(
            Arrays.asList(manager).iterator()
        );
        final Self core = Mockito.mock(Self.class);
        Mockito.when(core.projectManagers()).thenReturn(all);

        final AcceptInvitations component = new AcceptInvitations(core);
        component.acceptInvitations();

        for(final Invitation inv : mocks) {
            Mockito.verify(inv, Mockito.times(1)).accept();
        }
    }
}
