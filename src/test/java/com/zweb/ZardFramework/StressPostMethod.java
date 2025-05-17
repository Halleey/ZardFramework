package com.zweb.ZardFramework;

import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StressPostMethod {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        int totalRequests = 2000;

        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    long start = System.nanoTime();

                    URL url = new URL("http://localhost:8080/user/save"); // seu endpoint POST
                    int code = getCode(url);

                    long end = System.nanoTime();
                    long latencyMillis = (end - start) / 1_000_000;
                    latencies.add(latencyMillis);

                    System.out.println("Resposta: " + code + " | Latência: " + latencyMillis + " ms");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long total = latencies.stream().mapToLong(Long::longValue).sum();
        double avg = total / (double) latencies.size();
        long max = latencies.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = latencies.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("\n--- Estatísticas de Latência ---");
        System.out.println("Total de requisições: " + latencies.size());
        System.out.println("Média: " + avg + " ms");
        System.out.println("Mínima: " + min + " ms");
        System.out.println("Máxima: " + max + " ms");
    }

    private static int getCode(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true); // necessário para enviar corpo

        // Corpo JSON fictício
        String jsonInput = """
                    {  \s
                          "name": "zard",
                          "email":"aparece@gmail.com",
                          "cpf":"324564432-96",
                          "address_id": 2
                      }
                """;

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return conn.getResponseCode();
    }
}
