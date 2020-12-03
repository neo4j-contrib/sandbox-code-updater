package com.neo4j.sandbox.updater;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class HasherTest {

    @Test
    void consistently_hashes_folder_contents() throws Exception {
        Path path = testResourcesPath("/fake-template-repo").resolve("code");
        List<Path> files = asList(
                path.resolve("csharp").resolve("Example.cs"),
                path.resolve("java").resolve("Example.java"),
                path.resolve("python").resolve("example.py")
        );

        String hash = Hasher.hashFiles(files);

        assertThat(hash).isEqualTo("122e16666f937bd42ddecfb2fadc4be11505e35b517f8b19e5299a444877474f");
        assertThat(hash)
                .overridingErrorMessage("hash should be the same when the input is the same")
                .isEqualTo(Hasher.hashFiles(files));
    }

    private Path testResourcesPath(String resourceName) throws URISyntaxException {
        URL resource = this.getClass().getResource(resourceName);
        return new File(resource.toURI()).toPath();
    }
}