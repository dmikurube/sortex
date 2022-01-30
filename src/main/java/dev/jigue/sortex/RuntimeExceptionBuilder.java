/*
 * Copyright 2022 Dai MIKURUBE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jigue.sortex;

import java.util.function.Consumer;

final class RuntimeExceptionBuilder {
    RuntimeExceptionBuilder() {
        this.exception = null;
    }

    void add(final RuntimeException ex) {
        if (ex == null) {
            throw new NullPointerException("");
        }

        if (this.exception == null) {
            this.exception = ex;
        } else {
            this.exception.addSuppressed(ex);
        }
    }

    boolean isPresent() {
        return this.exception != null;
    }

    RuntimeException get() {
        return this.exception;
    }

    private RuntimeException exception;
}
