package io.keycafe.server;

import io.keycafe.server.services.BucketService;
import io.keycafe.server.services.CoorinationService;
import io.keycafe.server.services.Service;

public class Server {

    public static void main(String[] args) throws Exception {
        Service cacheService = new BucketService();
        cacheService.run();
//        Service coordination = new CoorinationService();
//        coordination.run();
    }
}
