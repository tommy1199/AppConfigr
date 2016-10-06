package io.github.tommy1199.appconfigr;

public abstract class VariableResolver {

    public String get(String variableName) {
        Result result = resolve(variableName);
        if (result instanceof Result.Some) {
            return result.get();
        } else {
            throw new ConfigurationException(((Result.None)result).getMsg());
        }
    }

    abstract Result resolve(String variableName);

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
     * Used internally as data structure if a resolver can resolve a variable.
     */
    public static abstract class Result {
        private Result() {
        }

        public abstract String get();

        public static Result some(String value) {
            return new Some(value);
        }

        public static Result none(String msg) {
            return new None(msg);
        }

        private static class Some extends Result{
            private final String value;

            private Some(String value) {
                this.value = value;
            }

            @Override
            public String get() {
                return value;
            }
        }

        private static class None extends Result{
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
