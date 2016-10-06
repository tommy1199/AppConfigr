package io.github.tommy1199.appconfigr;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VariablesTest {

    @Test
    public void shouldReturnAListOfVariablesFoundInAString() {
        String content = "${test}, ${blub}";

        List<Variables.Expression> variables = Variables.find(content);

        assertThat(variables).extracting("value", String.class)
                .containsExactly("test", "blub");
    }
}
