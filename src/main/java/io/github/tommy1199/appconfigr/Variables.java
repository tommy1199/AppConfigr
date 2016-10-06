package io.github.tommy1199.appconfigr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to extract variables from a string.
 */
class Variables {
    private static final String VAR_CAPTURE = "VAR";
    private static final String VAR_MATCHER = "\\$\\{(?<" + VAR_CAPTURE + ">[\\w\\.]+)}";

    private Variables() {
    }

    static List<Expression> find(String content) {
        List<Expression> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile(VAR_MATCHER);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(new Expression(matcher.group(VAR_CAPTURE)));
        }
        return variables;
    }

    static class Expression {
        private String value;

        public Expression(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expression that = (Expression) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        public String toString() {
            return "${" + value + "}";
        }
    }
}
