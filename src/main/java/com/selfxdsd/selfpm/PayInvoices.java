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
import com.selfxdsd.api.Invoice;
import com.selfxdsd.api.Payment;
import com.selfxdsd.api.Project;
import com.selfxdsd.api.ProjectManager;
import com.selfxdsd.api.Self;
import com.selfxdsd.api.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Each PM will periodically verify their Project Contract Invoices an try to
 * pay the ones that are eligible.
 * @author criske
 * @version $Id$
 * @since 0.0.6
 * @checkstyle NestedForDepth (500 lines)
 */
@Component
public final class PayInvoices {

    /**
     * The PMs will verify unpaid invoices and try to pay them every MONDAY.
     */
    private static final String EVERY_MONDAY = "0 0 * * MON";

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        PayInvoices.class
    );

    /**
     * Self's core.
     */
    private final Self selfCore;

    /**
     * Ctor.
     * @param selfCore Self Core, injected by Spring automatically.
     */
    @Autowired
    public PayInvoices(final Self selfCore) {
        this.selfCore = selfCore;
    }

    /**
     * Every Monday the PMs should verify their Project Contract Invoices an
     * try to pay the ones that are eligible.
     * @checkstyle IllegalCatch (50 lines)
     */
    @Scheduled(cron = EVERY_MONDAY)
    public void payInvoices() {
        LOG.debug("Checking invoices to be paid");
        for(final ProjectManager manager : this.selfCore.projectManagers()) {
            for(final Project project : manager.projects()) {
                final Wallet wallet = project.wallet();
                for(final Contract contact: project.contracts()){
                    for(final Invoice invoice: contact.invoices()){
                        if (!invoice.isPaid()
                            && invoice.totalAmount()
                            .longValueExact() >= 108 * 100) {
                            LOG.debug(
                                manager.username()
                                    + " is trying to pay invoice #"
                                    + invoice.invoiceId()
                                    + " for contract: "
                                    + contact.contractId()
                            );
                            try {
                                final Payment payment = wallet.pay(invoice);
                                LOG.debug("Payment finished with status: "
                                    + payment.status()
                                    + "(" + payment.failReason() + ")");
                            } catch (final Exception exception) {
                                LOG.error(
                                    "Payment failed due to an unexpected "
                                        + "error: "
                                        + exception.getClass()
                                        .getSimpleName()
                                        + "(" + exception.getMessage() + ")"
                                );
                            }
                            break;
                        }
                    }
                }
            }
        }
        LOG.debug("Done.");
    }
}