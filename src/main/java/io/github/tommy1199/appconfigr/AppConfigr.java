package io.github.tommy1199.appconfigr;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Access point for all configuration files in a specified folder. The basic usage is to create a new instance with one of the given factory methods.
 */
public class AppConfigr {

    private static final String DEFAULT_CONFIG_SUFFIX = ".conf";
    private static final String USER_DIR = "user.dir";
    private static final String DEFAULT_SUB_DIRECTORY = "config";
    private final Path basePath;
    private ObjectMapper mapper;
    private VariableResolver resolver;

    private AppConfigr(Path basePath, ObjectMapper mapper, VariableResolver resolver) {
        this.basePath = basePath;
        this.mapper = mapper;
        this.resolver = resolver;
    }

    /**
     * Creates a new instance with the given Path as base path for looking up configuration files.
     *
     * @param path the base path for looking up the configuration files.
     * @throws NullPointerException if the given path is {@code null}
     */
    public static Builder fromDirectory(Path path) {
        return new Builder(checkNotNull(path));
    }

    /**
     * Creates a new instance with the given File as base path for looking up configuration files.
     *
     * @param file the base path for looking up the configuration files.
     * @throws NullPointerException if the given path is {@code null}
     */
    public static Builder fromDirectory(File file) {
        checkNotNull(file);
        return fromDirectory(file.toPath());
    }

    /**
     * Creates a new instance with the given String as base path for looking up configuration files.
     *
     * @param path the base path as String for looking up the configuration files.
     * @throws NullPointerException if the given path is {@code null}
     */
    public static Builder fromDirectory(String path) {
        checkNotNull(path);
        return fromDirectory(Paths.get(path));
    }

    /**
     * Creates a new instance with a default directory as base path for looking up configuration files. The default
     * directory is "{user.dir}/config".
     *
     * @throws NullPointerException if the given path is {@code null}
     */
    public static Builder fromDefaultDirectory() {
        Path defaultPath = Paths.get(System.getProperty(USER_DIR), DEFAULT_SUB_DIRECTORY)
                .toAbsolutePath()
                .normalize();
        return fromDirectory(defaultPath);
    }

    /**
     * Loads configuration data from a file name derived from the class name. If the config class is called MyConfig
     * .class the default file name is the lowercase hyphenated class name with the default file suffix, in this case
     * my-config.conf.
     *
     * @param clazz the class used for the mapping and for deriving the filename
     * @return the loaded configuration data
     * @throws IllegalArgumentException if the file cannot be found in the base path.
     */
    public <T> T getConfig(Class<T> clazz) {
        return getConfig(clazz, toFileName(clazz));
    }

    /**
     * Loads configuration data from file with the given filename.
     *
     * @param clazz    the class used for the mapping
     * @param fileName the file name to be loaded
     * @return the loaded configuration data
     * @throws IllegalArgumentException if the file cannot be found in the base path.
     */
    public <T> T getConfig(Class<T> clazz, String fileName) {
        Path fullPath = getAbsoluteFilePath(fileName);
        checkExists(fullPath);
        T config = null;
        try {
            config = createConfig(clazz, fullPath);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return config;
    }

    private <T> T createConfig(Class<T> clazz, Path fullPath) throws IOException {
        String raw = readContent(fullPath);
        String content = replaceVariables(raw);
        return mapper.readValue(content, clazz);
    }

    private String replaceVariables(String content) throws IOException {
        String out = content;
        List<Variables.Expression> variables = Variables.find(content);
        for (Variables.Expression variable : variables) {
            String value = resolver.get(variable.getValue());
            out = out.replaceAll(Pattern.quote(variable.toString()), value);
        }
        return out;
    }

    private String readContent(Path filePath) throws IOException {
        byte[] allBytes = Files.readAllBytes(filePath);
        return new String(allBytes, Charsets.UTF_8);
    }

    private void checkExists(Path filePath) {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("The given file can not be found [" + filePath + "]");
        }
    }

    private String toFileName(Class<?> clazz) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, clazz.getSimpleName()) + DEFAULT_CONFIG_SUFFIX;
    }

    private Path getAbsoluteFilePath(String fileName) {
        return Paths.get(basePath.toString(), fileName);
    }

    /**
     * Creates a AppConfigr instance.
     */
    public static class Builder {
        private final Path path;
        private boolean checkDirectory = true;
        private JsonFactory factory = new YAMLFactory();
        private VariableResolver resolver = VariableResolver.fromSystemProperties()
                .withFallback(VariableResolver.fromEnvironment());


        private Builder(Path path) {
            this.path = path;
        }


        /**
         * Will suppress the check if the given path is a valid directory. The validation will be done when the first
         * configuration is requested by the built AppConfigr instance.
         *
         * @return this builder.
         */
        public Builder noCheck() {
            checkDirectory = false;
            return this;
        }

        /**
         * Replaces the factory used by AppConfigr. The standard format used for the configuration files is yaml and
         * can be replaced with any dataformat supported by Jackson.
         *
         * @return this builder
         * @throws NullPointerException if the given factory is {@code null}
         */
        public Builder withFactory(JsonFactory factory) {
            this.factory = checkNotNull(factory, "The given mapper factory must not be null");
            return this;
        }

        /**
         * Changes the way how variables in the configuration files will be resolved.
         *
         * @throws NullPointerException if the given resolver is {@code null}
         */
        public Builder withResolvingStrategy(VariableResolver resolver) {
            this.resolver = checkNotNull(resolver, "The given resolver must not be null");
            return this;
        }

        /**
         * Creates a new instance of AppConfigr.
         */
        public AppConfigr build() {
            validate();
            return new AppConfigr(path, new ObjectMapper(factory), resolver);
        }

        private void validate() {
            if (checkDirectory && !Files.isDirectory(path)) {
                throw new IllegalArgumentException("The given path is not a valid directory [" + path + "]. This " +
                        "exception can be suppressed by calling noCheck() on Builder.");
            }
        }
    }
}
