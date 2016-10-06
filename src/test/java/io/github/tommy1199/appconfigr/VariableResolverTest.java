package io.github.tommy1199.appconfigr;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

public class VariableResolverTest {

    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @Rule
    public final ProvideSystemProperty property1 = new ProvideSystemProperty("FROM_PROPS", "fromprops");

    @Test(expected = ConfigurationException.class)
    public void shouldThrowConfigurationExceptionWhenVariableCannotBeResolvedByEnv() {
        VariableResolver.fromEnvironment()
                .get("test");
    }

    @Test
    public void shouldResolveFromEnvironmentVariable() {
        envs.set("TEST_PROPERTY", "value");
        String cut = VariableResolver.fromEnvironment()
                .get("TEST_PROPERTY");
        assertThat(cut).isEqualTo("value");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenGivenVariableNameIsNull() {
        VariableResolver.fromSystemProperties()
                .get(null);
    }

    @Test(expected = ConfigurationException.class)
    public void shouldThrowConfigurationExceptionWhenVariableCannotBeResolvedBySystemProperties() {
        VariableResolver.fromSystemProperties()
                .get("test");
    }

    @Test
    public void shouldResolveFromSystemProperties() {
        String cut = VariableResolver.fromSystemProperties()
                .get("FROM_PROPS");
        assertThat(cut).isEqualTo("fromprops");
    }

    @Test
    public void shouldUseFallbackWhenOriginalCannotResolve() {
        String cut = VariableResolver.fromEnvironment()
                .withFallback(VariableResolver.fromSystemProperties())
                .get("FROM_PROPS");

        assertThat(cut).isEqualTo("fromprops");
    }

    @Test(expected = ConfigurationException.class)
    public void shouldThrowConfigurationExceptionWhenFallbackCannotResolve() {
        VariableResolver.fromEnvironment()
                .withFallback(VariableResolver.fromSystemProperties())
                .get("Unknown");
    }
}
