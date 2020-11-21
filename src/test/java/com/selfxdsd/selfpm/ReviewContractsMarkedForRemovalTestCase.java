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
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 * Unit tests for {@link ReviewContractsMarkedForRemoval}.
 * @author criske
 * @version $Id$
 * @since 0.0.4
 */
public final class ReviewContractsMarkedForRemovalTestCase {

    /**
     * It should look for project contracts marked for removal and remove
     * the ones that were marked more than 30 days.
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @Test
    public void reviewsAndRemovesContractsMarkedForRemoval() {
        final LocalDateTime now = LocalDateTime.now();

        final Self self = Mockito.mock(Self.class);

        final ProjectManagers managers = Mockito.mock(ProjectManagers.class);
        Mockito.when(self.projectManagers()).thenReturn(managers);

        final ProjectManager manager = Mockito.mock(ProjectManager.class);

        final List<ProjectManager> managersSrc = List.of(manager);
        Mockito.when(managers.iterator())
            .thenReturn(managersSrc.iterator());

        final Projects projects = Mockito.mock(Projects.class);
        Mockito.when(manager.projects()).thenReturn(projects);

        final Project project = Mockito.mock(Project.class);

        final List<Project> projectsSrc = List.of(project);
        Mockito.when(projects.iterator()).thenReturn(projectsSrc.iterator());

        final Contracts contracts = Mockito.mock(Contracts.class);
        Mockito.when(project.contracts()).thenReturn(contracts);
        final Contract.Id contractIdA = new Contract.Id(
            "john/test",
            "john",
            "github",
            "PO"
        );
        final Contract.Id contractIdB = new Contract.Id(
            "john/test",
            "mark",
            "github",
            "DEV"
        );
        final Contract.Id contractIdC = new Contract.Id(
            "john/test",
            "steve",
            "github",
            "DEV"
        );
        Contract ctA = this.mockContract(contractIdA, null);
        Contract ctB = this.mockContract(contractIdB, this.daysAgo(now, 31));
        Contract ctC = this.mockContract(contractIdC, this.daysAgo(now, 30));
        final List<Contract> contractsSrc = List.of(ctA, ctB, ctC);
        Mockito.when(contracts.iterator())
            .thenReturn(contractsSrc.iterator());

        new ReviewContractsMarkedForRemoval(self, () -> now)
            .reviewContractsMarkedForRemoval();

        Mockito.verify(ctA, Mockito.never()).remove();
        Mockito.verify(ctB, Mockito.times(1)).remove();
        Mockito.verify(ctC, Mockito.never()).remove();
    }

    /**
     * Mocks a Contract for a Project.
     * @param contractId Contract id.
     * @param markedForRemoval Date of marking the removal or null.
     * @return Contract.
     */
    private Contract mockContract(final Contract.Id contractId,
                                  final LocalDateTime markedForRemoval){
        final Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.contractId()).thenReturn(contractId);
        Mockito.when(contract.markedForRemoval()).thenReturn(markedForRemoval);
        return contract;
    }

    /**
     * LocalDateTime of days ago from now.
     * @param now LocalDateTime.
     * @param days Days.
     * @return LocalDateTime.
     */
    private LocalDateTime daysAgo(final LocalDateTime now, final int days){
        return now.minus(Period.ofDays(days));
    }
}
