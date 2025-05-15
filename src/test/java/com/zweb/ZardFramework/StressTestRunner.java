package com.zweb.ZardFramework;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StressTestRunner {
	public static void main(String[] args) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(50);
		int totalRequests = 2000;

		List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

		for (int i = 0; i < totalRequests; i++) {
			executor.submit(() -> {
				try {
					long start = System.nanoTime();

					URL url = new URL("http://localhost:8080/user/todos");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					int code = conn.getResponseCode();

					long end = System.nanoTime();
					long latencyMillis = (end - start) / 1_000_000; // converte para milissegundos
					latencies.add(latencyMillis);

					System.out.println("Resposta: " + code + " | Latência: " + latencyMillis + " ms");

				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);

// Estatísticas (pode ser após o shutdown):
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

	}
