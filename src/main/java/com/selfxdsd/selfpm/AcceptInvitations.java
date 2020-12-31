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

import com.selfxdsd.api.Invitation;
import com.selfxdsd.api.Invitations;
import com.selfxdsd.api.ProjectManager;
import com.selfxdsd.api.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Each PM will periodically check their repo Invitations and accept any
 * they might have received.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.4
 */
@Component
public final class AcceptInvitations {

    /**
     * The PMs will verify their invitations every 10 minutes.
     */
    private static final int EVERY_10_MINUTES = 600000;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        AcceptInvitations.class
    );

    /**
     * Self's core.
     */
    private final Self selfCore;

    /**
     * Ctor.
     * @param selfCode Self Core, injected by Spring automatically.
     */
    @Autowired
    public AcceptInvitations(final Self selfCode) {
        this.selfCore = selfCode;
    }

    /**
     * Every 10 minutes the PMs should verify their
     * Invitations and accept them.
     */
    @Scheduled(fixedRate = EVERY_10_MINUTES)
    public void acceptInvitations() {
        LOG.debug("Checking invitations of PMs...");
        for(final ProjectManager manager : this.selfCore.projectManagers()) {
            final Invitations invitations = manager.provider().invitations();
            for(final Invitation invitation : invitations) {
                LOG.debug(
                    manager.username()
                    + " accepting Invitation "
                    + invitation.json()
                );
                invitation.accept();
                LOG.debug("Invitation accepted.");
            }
        }
        LOG.debug("Done.");
    }

}
