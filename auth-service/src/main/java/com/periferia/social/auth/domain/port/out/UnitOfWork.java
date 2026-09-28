package com.periferia.social.auth.domain.port.out;

import java.util.function.Supplier;

/** Límite transaccional expresado como puerto, para no acoplar la aplicación a Spring. */
public interface UnitOfWork {

    <T> T inTransaction(Supplier<T> work);
}
