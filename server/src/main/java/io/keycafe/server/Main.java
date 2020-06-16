package io.keycafe.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java -jar server.jar <config-file-name>.yaml");
            System.exit(1);
        }
        String configFileName = args[0];
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final File configFile = Paths.get(configFileName).toFile();
        Configuration config = mapper.readValue(configFile, Configuration.class);
        Map<String, String> options = System.getenv();

        Server server = new Server(config, options);
        server.run();
    }
}
