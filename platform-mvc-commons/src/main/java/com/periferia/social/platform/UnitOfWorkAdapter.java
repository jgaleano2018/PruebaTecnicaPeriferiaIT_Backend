package com.periferia.social.platform;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Implementación basada en Spring de la unidad de trabajo transaccional. Cada servicio
 * expone su propio puerto de salida ({@code UnitOfWork}) y lo implementa delegando aquí,
 * de modo que la capa de aplicación no depende de Spring.
 */
@Component
public class UnitOfWorkAdapter {

    private final TransactionTemplate transactionTemplate;

    public UnitOfWorkAdapter(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
