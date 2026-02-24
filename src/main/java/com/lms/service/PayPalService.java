package com.lms.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    private final APIContext apiContext;

    public PayPalService(APIContext apiContext) {
        this.apiContext = apiContext;
    }

    public Payment createPayment(Double total, String currency, String method,
                                 String intent, String description, String cancelUrl,
                                 String successUrl) throws PayPalRESTException {
        try {
            Amount amount = new Amount();
            amount.setCurrency(currency);
            amount.setTotal(String.format("%.2f", total));

            Transaction transaction = new Transaction();
            transaction.setDescription(description);
            transaction.setAmount(amount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod(method);

            Payment payment = new Payment();
            payment.setIntent(intent);
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(cancelUrl);
            redirectUrls.setReturnUrl(successUrl);
            payment.setRedirectUrls(redirectUrls);

            return payment.create(apiContext);
        } catch (Exception e) {
            throw new PayPalRESTException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        try {
            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            return payment.execute(apiContext, paymentExecution);
        } catch (Exception e) {
            throw new PayPalRESTException("Failed to execute payment: " + e.getMessage(), e);
        }
    }

    public Payment getPaymentDetails(String paymentId) throws PayPalRESTException {
        try {
            return Payment.get(apiContext, paymentId);
        } catch (Exception e) {
            throw new PayPalRESTException("Failed to get payment details: " + e.getMessage(), e);
        }
    }
}