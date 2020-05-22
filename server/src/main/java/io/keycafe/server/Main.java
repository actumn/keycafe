package io.keycafe.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.keycafe.server.config.Configuration;

import java.io.File;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final File configFile = Paths.get("config3.yaml").toFile();
        Configuration config = mapper.readValue(configFile, Configuration.class);

        Server server = new Server(config);
        server.run();
    }
}
