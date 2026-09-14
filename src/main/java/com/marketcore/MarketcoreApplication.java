package com.marketcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarketcoreApplication {
	public static void main(String[] args) {

		String senha = System.getenv("MARKETCORE_DB_PASSWORD");

		System.out.println("TAMANHO: " + senha.length());
		System.out.println("TAMANHO COM STRIP: " + senha.strip().length());
		System.out.println("TEM ESPACO NAS PONTAS: " + !senha.equals(senha.strip()));
		System.out.println("COMECA COM ASPAS: " + senha.startsWith("\""));
		System.out.println("TERMINA COM ASPAS: " + senha.endsWith("\""));
		SpringApplication.run(MarketcoreApplication.class, args);
	}
}
