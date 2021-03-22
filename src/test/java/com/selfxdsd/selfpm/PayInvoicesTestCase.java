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

import com.selfxdsd.api.Contract;
import com.selfxdsd.api.Contracts;
import com.selfxdsd.api.Invoice;
import com.selfxdsd.api.Invoices;
import com.selfxdsd.api.Payment;
import com.selfxdsd.api.Project;
import com.selfxdsd.api.ProjectManager;
import com.selfxdsd.api.ProjectManagers;
import com.selfxdsd.api.Projects;
import com.selfxdsd.api.Self;
import com.selfxdsd.api.Wallet;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Unit tests for {@link PayInvoices}.
 *
 * @author criske
 * @version $Id$
 * @since 0.0.6
 * @checkstyle ExecutableStatementCount (500 lines)
 */
public final class PayInvoicesTestCase {

    /**
     * It should successfully pay an active invoice.
     */
    @Test
    public void paysActiveInvoice() {
        final Self self = Mockito.mock(Self.class);
        final ProjectManagers managers = Mockito.mock(ProjectManagers.class);
        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = Mockito.mock(Project.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract contract = Mockito.mock(Contract.class);
        final Contract.Id contractId = new Contract.Id(
            "john/test",
            "test",
            "github",
            "dev"
        );
        final Invoices invoices = Mockito.mock(Invoices.class);

        Mockito.when(self.projectManagers()).thenReturn(managers);
        this.mockIterator(managers, manager);
        Mockito.when(manager.projects()).thenReturn(projects);
        this.mockIterator(projects, project);
        Mockito.when(manager.username()).thenReturn("zoe");
        this.mockIterator(projects, project);
        Mockito.when(project.wallet()).thenReturn(wallet);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contract.contractId()).thenReturn(contractId);
        this.mockIterator(contracts, contract);
        Mockito.when(contract.invoices()).thenReturn(invoices);
        final Invoice active = this.mockInvoice(1, false);
        Mockito.when(active.totalAmount())
            .thenReturn(BigDecimal.valueOf(200 * 100));
        final Payment payment = Mockito.mock(Payment.class);
        Mockito.when(payment.status()).thenReturn(Payment.Status.SUCCESSFUL);
        Mockito.when(wallet.pay(active)).thenReturn(payment);
        this.mockIterator(
            invoices,
            active,
            this.mockInvoice(2, true),
            this.mockInvoice(3, true),
            this.mockInvoice(4, true),
            this.mockInvoice(5, true)
        );

        new PayInvoices(self).payInvoices();

        Mockito.verify(wallet, Mockito.times(1))
            .pay(Mockito.any());
        Mockito.verify(payment, Mockito.times(1))
            .status();
    }

    /**
     * It should skip paying when active amount is too low (< 108EUR).
     */
    @Test
    public void skipsPayingWhenActiveInvoiceAmountIsTooLow() {
        final Self self = Mockito.mock(Self.class);
        final ProjectManagers managers = Mockito.mock(ProjectManagers.class);
        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = Mockito.mock(Project.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract contract = Mockito.mock(Contract.class);
        final Contract.Id contractId = new Contract.Id(
            "john/test",
            "test",
            "github",
            "dev"
        );
        final Invoices invoices = Mockito.mock(Invoices.class);

        Mockito.when(self.projectManagers()).thenReturn(managers);
        this.mockIterator(managers, manager);
        Mockito.when(manager.projects()).thenReturn(projects);
        Mockito.when(manager.username()).thenReturn("zoe");
        this.mockIterator(projects, project);
        Mockito.when(project.wallet()).thenReturn(wallet);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contract.contractId()).thenReturn(contractId);
        this.mockIterator(contracts, contract);
        Mockito.when(contract.invoices()).thenReturn(invoices);
        final Invoice active = this.mockInvoice(1, false);
        Mockito.when(active.totalAmount())
            .thenReturn(BigDecimal.TEN);
        this.mockIterator(
            invoices,
            active,
            this.mockInvoice(2, true),
            this.mockInvoice(3, true),
            this.mockInvoice(4, true),
            this.mockInvoice(5, true)
        );

        new PayInvoices(self).payInvoices();

        Mockito.verify(wallet, Mockito.never())
            .pay(Mockito.any());
    }

    /**
     * It should fail paying an active invoice to payment error
     * (Payment.Status.ERROR).
     */
    @Test
    public void failsPayingActiveInvoice() {
        final Self self = Mockito.mock(Self.class);
        final ProjectManagers managers = Mockito.mock(ProjectManagers.class);
        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = Mockito.mock(Project.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract contract = Mockito.mock(Contract.class);
        final Contract.Id contractId = new Contract.Id(
            "john/test",
            "test",
            "github",
            "dev"
        );
        final Invoices invoices = Mockito.mock(Invoices.class);

        Mockito.when(self.projectManagers()).thenReturn(managers);
        this.mockIterator(managers, manager);
        Mockito.when(manager.projects()).thenReturn(projects);
        Mockito.when(manager.username()).thenReturn("zoe");
        this.mockIterator(projects, project);
        Mockito.when(project.wallet()).thenReturn(wallet);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contract.contractId()).thenReturn(contractId);
        this.mockIterator(contracts, contract);
        Mockito.when(contract.invoices()).thenReturn(invoices);
        final Invoice active = this.mockInvoice(1, false);
        Mockito.when(active.totalAmount())
            .thenReturn(BigDecimal.valueOf(200 * 100));
        final Payment payment = Mockito.mock(Payment.class);
        Mockito.when(payment.status()).thenReturn(Payment.Status.ERROR);
        Mockito.when(wallet.pay(active)).thenReturn(payment);
        this.mockIterator(
            invoices,
            active,
            this.mockInvoice(2, true),
            this.mockInvoice(3, true),
            this.mockInvoice(4, true),
            this.mockInvoice(5, true)
        );

        new PayInvoices(self).payInvoices();

        Mockito.verify(wallet, Mockito.times(1))
            .pay(active);
        Mockito.verify(payment, Mockito.times(1))
            .status();
    }

    /**
     * It should fail paying an active invoice to an unexpected Expception.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void failsPayingActiveInvoiceDueToException() {
        final Self self = Mockito.mock(Self.class);
        final ProjectManagers managers = Mockito.mock(ProjectManagers.class);
        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = Mockito.mock(Project.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract contract = Mockito.mock(Contract.class);
        final Contract.Id contractId = new Contract.Id(
            "john/test",
            "test",
            "github",
            "dev"
        );
        final Invoices invoices = Mockito.mock(Invoices.class);

        Mockito.when(self.projectManagers()).thenReturn(managers);
        this.mockIterator(managers, manager);
        Mockito.when(manager.projects()).thenReturn(projects);
        Mockito.when(manager.username()).thenReturn("zoe");
        this.mockIterator(projects, project);
        Mockito.when(project.wallet()).thenReturn(wallet);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contract.contractId()).thenReturn(contractId);
        this.mockIterator(contracts, contract);
        Mockito.when(contract.invoices()).thenReturn(invoices);
        final Invoice active = this.mockInvoice(1, false);
        Mockito.when(active.totalAmount())
            .thenReturn(BigDecimal.valueOf(200 * 100));
        final Payment payment = Mockito.mock(Payment.class);
        Mockito.when(wallet.pay(active))
            .thenThrow(UnsupportedOperationException.class);
        this.mockIterator(
            invoices,
            active,
            this.mockInvoice(2, true),
            this.mockInvoice(3, true),
            this.mockInvoice(4, true),
            this.mockInvoice(5, true)
        );

        new PayInvoices(self).payInvoices();

        Mockito.verify(payment, Mockito.never())
            .status();
    }

    /**
     * Mocks Iterable's Iterator.
     * @param iterable Iterable.
     * @param items Items.
     * @param <T> Iterable type.
     * @param <E> Iterable element type.
     */
    @SafeVarargs
    private <T extends Iterable<E>, E> void mockIterator(
        final T iterable,
        final E... items) {
        Mockito.when(iterable.iterator())
            .thenReturn(Arrays.asList(items).iterator());
    }

    /**
     * Mocks an Invoice.
     * @param id Id.
     * @param isPaid Boolean.
     * @return Invoice.
     */
    private Invoice mockInvoice(final int id, final boolean isPaid){
        Invoice invoice = Mockito.mock(Invoice.class);
        Mockito.when(invoice.invoiceId()).thenReturn(id);
        Mockito.when(invoice.isPaid()).thenReturn(isPaid);
        return invoice;
    }
}