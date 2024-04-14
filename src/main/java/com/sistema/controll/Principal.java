package com.sistema.controll;

import com.sistema.service.ConexaoJPA;

public class Principal {

	private static ConexaoJPA conexaoJPA = null;

	public static void main(String[] args) {
		conexaoJPA = new ConexaoJPA("persistenceJPA");

	}

	public static ConexaoJPA getConexaoJPA() {
		return conexaoJPA;
	}

}
