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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Each PM will periodically review the contracts for removal from the projects
 * they manage.
 * @author criske
 * @version $Id$
 * @since 0.0.4
 * @checkstyle IllegalCatch (1000 lines)
 */
@Component
public final class ReviewContractsMarkedForRemoval {

    /**
     * The PMs will review contracts every day.
     */
    private static final long EVERY_24_HOURS = 86_400_000L;


    /**
     * The PMs will review contracts every day with a start delay of 15 minutes.
     */
    private static final long DELAY_15_MINUTES = 900_000L;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        ReviewContractsMarkedForRemoval.class
    );

    /**
     * Self's core.
     */
    private final Self selfCore;

    /**
     * Time "now" supplier that will be used to determined how many days
     * ago a Contract marked for removal.
     */
    private final Supplier<LocalDateTime> nowSupplier;

    /**
     * Ctor.
     * @param selfCore Self Core, injected by Spring automatically.
     */
    @Autowired
    public ReviewContractsMarkedForRemoval(final Self selfCore) {
        this(selfCore, LocalDateTime::now);
    }

    /**
     * Ctor used in tests.
     * @param selfCore Self Core.
     * @param nowSupplier Time "now" supplier.
     */
    ReviewContractsMarkedForRemoval(final Self selfCore,
                                    final Supplier<LocalDateTime> nowSupplier){
        this.selfCore = selfCore;
        this.nowSupplier = nowSupplier;
    }

    /**
     * Every 24 hours the PMs should verify contracts marked for removal.
     * <br/>
     * Contracts marked for removal more than 30 days ago will be removed.
     * <br/>
     * It also has start of 15 minutes delay, so it will not overlap with
     * other scheduled jobs.
     * @checkstyle NestedForDepth (50 lines).
     */
    @Scheduled(fixedRate = EVERY_24_HOURS, initialDelay = DELAY_15_MINUTES)
    public void reviewContractsMarkedForRemoval() {
        LOG.debug("PMs reviewing project contracts marked for removal...");
        for(final ProjectManager manager : this.selfCore.projectManagers()) {
            LOG.debug(
                "PM @" + manager.username()
                + " reviewing their project contracts marked for removal..."
            );
            for(final Project project : manager.projects()) {
                LOG.debug(
                    "Reviewing contracts marked for removal from project "
                    + project.repoFullName() + " at " + project.provider()
                    + "... "
                );
                final List<Contract> toRemove = this.contractsToRemove(
                    project
                );
                LOG.debug(
                    "For project " + project.repoFullName() + " at "
                    + project.provider() + " there are " + toRemove.size()
                    + " contracts that will be removed..."
                );
                for(final Contract contract: toRemove){
                    LOG.debug(
                        "Removing contract ["
                        + contract.contractId() + "]..."
                    );
                    try {
                        contract.remove();
                        LOG.debug("Contract successfully removed!");
                    } catch (final RuntimeException ex) {
                        LOG.error(
                            "Problem while removing contract ["
                            + contract.contractId() + "].",
                            ex
                        );
                    }
                }
            }
        }
        LOG.debug("All PMs finished reviewing their marked for removal "
            + "project contracts");
    }


    /**
     * Project contracts that were marked for removal 30+ days ago.
     * @param project Project.
     * @return List of contracts to be removed.
     */
    private List<Contract> contractsToRemove(final Project project){
        final LocalDateTime now = this.nowSupplier.get();
        final Contracts contracts = project.contracts();
        final List<Contract> toBeRemoved = new ArrayList<>();
        for (final Contract contract: contracts){
            final LocalDateTime markedForRemoval = contract.markedForRemoval();
            if (markedForRemoval != null
                && markedForRemoval.until(now, ChronoUnit.DAYS) > 30) {
                toBeRemoved.add(contract);
            }
        }
        return toBeRemoved;
    }
}
