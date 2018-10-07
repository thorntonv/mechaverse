package org.mechaverse.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.mechaverse.manager.api.MechaverseManagerApi;
import org.mechaverse.manager.client.spring.MechaverseManagerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({MechaverseManagerClientConfig.class})
@EnableAutoConfiguration
public class MechaverseClientApp implements CommandLineRunner {
 
    private static Logger logger = LoggerFactory.getLogger(MechaverseClientApp.class);

    @Autowired
    private MechaverseManagerApi manager;

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("Missing argument: clientId");
            return;
        }
        SpringApplication.run(MechaverseClientApp.class, args);
    }
  
    @Override
    public void run(String... args) throws InterruptedException {
        String clientId = args[0];

        final List<MechaverseClient> clientInstances = new ArrayList<>();
        int instanceCount = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(instanceCount);
        for (int idx = 0; idx < instanceCount; idx++) {
            final int instanceIdx = idx;
            executorService.submit(() -> {
                MechaverseClient clientInstance = new MechaverseClient(
                    clientId, manager, instanceIdx);
                clientInstances.add(clientInstance);
                clientInstance.start();
            });
        }

        executorService.shutdown();
        while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) { }
    }
}