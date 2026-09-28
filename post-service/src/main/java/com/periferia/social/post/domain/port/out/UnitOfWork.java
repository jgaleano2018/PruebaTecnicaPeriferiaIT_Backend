package com.periferia.social.post.domain.port.out;

import java.util.function.Supplier;

public interface UnitOfWork {

    <T> T inTransaction(Supplier<T> work);
}
