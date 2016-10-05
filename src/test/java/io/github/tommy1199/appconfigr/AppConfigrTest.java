package io.github.tommy1199.appconfigr;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class AppConfigrTest {

    @Test
    public void shouldThrowNullPointerExIfGivenPathIsNull() {
        try {
            AppConfigr.fromDirectory((Path) null);
            fail("NullPointerException expected when calling fromDirectory with [null] parameter.");
        } catch (NullPointerException e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void shouldThrowNullPointerExIfGivenFileIsNull() {
        try {
            AppConfigr.fromDirectory((File) null);
            fail("NullPointerException expected when calling fromDirectory with [null] parameter.");
        } catch (NullPointerException e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void shouldThrowNullPointerExIfGivenStringPathIsNull() {
        try {
            AppConfigr.fromDirectory((String) null);
            fail("NullPointerException expected when calling fromDirectory with [null] parameter.");
        } catch (NullPointerException e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExIfGivenPathIsNotARealDirectory() {
        try {
            AppConfigr.fromDirectory(Paths.get("unknown/folder"))
                    .build();
            fail("IllegalArgumentException expected when calling build with not existing directory.");
        } catch (IllegalArgumentException e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void shouldSuppressDirectoryCheckIfDeactivatedInBuilder() {
        AppConfigr.fromDirectory(Paths.get("unknown/folder"))
                .noCheck()
                .build();
    }

    @Test
    public void shouldBuildIfPathExist() {
        Path sampleConfigDir = new File(this.getClass()
                .getResource("/sample-configs")
                .getFile()).toPath();
        AppConfigr.fromDirectory(sampleConfigDir)
                .noCheck()
                .build();
    }

    @Test
    public void shouldBuildIfDirFileExist() {
        File sampleConfigDir = new File(this.getClass()
                .getResource("/sample-configs")
                .getFile());
        AppConfigr.fromDirectory(sampleConfigDir)
                .noCheck()
                .build();
    }

    @Test
    public void shouldBuildIfStringPathExist() {
        String sampleConfigDir = new File(this.getClass()
                .getResource("/sample-configs")
                .getFile()).getAbsolutePath();
        AppConfigr.fromDirectory(sampleConfigDir)
                .noCheck()
                .build();
    }
}