package io.github.tommy1199.appconfigr;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Access point for all configuration files in a specified folder. The basic usage is to create a new instance with one of the given factory methods.
 */
public class AppConfigr {

    private static final String USER_DIR = "user.dir";
    private static final String DEFAULT_SUB_DIRECTORY = "config";
    private final Path basePath;
    private ObjectMapper mapper;

    private AppConfigr(Path basePath, ObjectMapper mapper) {
        this.basePath = basePath;
        this.mapper = mapper;
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

    public static class Builder {
        private final Path path;
        private boolean checkDirectory = true;
        private JsonFactory factory = new YAMLFactory();


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
         * @throws NullPointerException if the given factory is {@code null}
         *
         * @return this builder
         */
        public Builder withFactory(JsonFactory factory) {
            this.factory = checkNotNull(factory, "The given mapper factory must not be null");
            return this;
        }

        public AppConfigr build() {
            validate();
            return new AppConfigr(path, new ObjectMapper(factory));
        }

        private void validate() {
            if (checkDirectory && !Files.isDirectory(path)) {
                throw new IllegalArgumentException("The given path is not a valid directory [" + path + "]. This " +
                        "exception can be suppressed by calling noCheck() on Builder.");
            }
        }
    }
}
