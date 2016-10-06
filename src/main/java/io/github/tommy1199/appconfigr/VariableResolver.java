package io.github.tommy1199.appconfigr;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Returns a value for a given variable and is used by the {@link AppConfigr} to resolve variables in the
 * configuration files. It provides ready to use providers based on environment variables and system properties.
 */
public abstract class VariableResolver {

    /**
     * Returns a VariableResolver which is based on environment variables.
     *
     * @return a resolver which is backed by environment variables.
     */
    public static VariableResolver fromEnvironment() {
        return new VariableResolver() {
            @Override
            Result resolve(String variableName) {
                String result = System.getenv(variableName);
                if (result != null) {
                    return Result.some(result);
                } else {
                    return Result.none("[" + variableName + "] can not be resolved from the environment variables.");
                }
            }
        };
    }

    /**
     * Returns a VariableResolver which is based on system properties. This VariableResolver is case-sensitive.
     *
     * @return a resolver which is backed by system properties.
     */
    public static VariableResolver fromSystemProperties() {
        return new VariableResolver() {
            @Override
            Result resolve(String variableName) {
                String result = System.getProperty(variableName);
                if (result != null) {
                    return Result.some(result);
                } else {
                    return Result.none("[" + variableName + "] can not be resolved from the system properties.");
                }
            }
        };
    }

    /**
     * Returns the resolved value for the given variable name.
     *
     * @return the resolved value for the given variableName.
     * @throws NullPointerException if the given variableName is {@code null}
     */
    final public String get(String variableName) {
        checkNotNull(variableName, "Variable Name must not be null");

        Result result = resolve(variableName);
        if (result instanceof Result.Some) {
            return result.get();
        } else {
            throw new ConfigurationException(((Result.None) result).getMsg());
        }
    }

    /**
     * Should be implemented by subclasses. This method should not throw any exception, but should return a
     * {@link Result.None} with a describing message instead.
     */
    abstract Result resolve(String variableName);

    /**
     * Returns a new VariableResolver which uses first {@code this} to resolve the variable than the fallback
     * resolver. If none of them can resolve the variable a {@link ConfigurationException} is thrown with the
     * combined messages of both internal resolvers.
     *
     * @return a resolver which first use {@code this}, then the fallback resolver
     */
    public VariableResolver withFallback(VariableResolver fallback) {
        return new WithFallbackResolver(this, fallback);
    }

    private static class WithFallbackResolver extends VariableResolver {
        private final VariableResolver original;
        private final VariableResolver fallback;

        private WithFallbackResolver(VariableResolver original, VariableResolver fallback) {
            this.original = original;
            this.fallback = fallback;
        }

        @Override
        Result resolve(String variableName) {
            Result result = original.resolve(variableName);
            if (result instanceof Result.Some) {
                return result;
            } else {
                return fallback(variableName, ((Result.None) result).getMsg());
            }
        }

        Result fallback(String variableName, String originalMsg) {
            Result result = fallback.resolve(variableName);
            if (result instanceof Result.Some) {
                return result;
            } else {
                return Result.none(originalMsg + " " + ((Result.None) result).getMsg());
            }
        }
    }


    /**
     * Used internally as data structure for results of an resolver.
     */
    public static abstract class Result {
        private Result() {
        }

        public static Result some(String value) {
            return new Some(value);
        }

        public static Result none(String msg) {
            return new None(msg);
        }

        public abstract String get();

        private static class Some extends Result {
            private final String value;

            private Some(String value) {
                this.value = value;
            }

            @Override
            public String get() {
                return value;
            }
        }

        private static class None extends Result {
            private final String msg;

            private None(String msg) {
                this.msg = msg;
            }

            @Override
            public String get() {
                throw new UnsupportedOperationException("Not allowed for a None result.");
            }

            public String getMsg() {
                return msg;
            }
        }
    }
}