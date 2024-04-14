package com.sistema.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ConexaoJPA {

	private EntityManagerFactory emf = null;
	private EntityManager em = null;

	public EntityManager getEntityManager() {
		return em;
	}

	public ConexaoJPA(String nomePersistence) {
		this.emf = Persistence.createEntityManagerFactory(nomePersistence);
		this.em = emf.createEntityManager();
		System.out.println("Conectou com o JPA!!");
	}

	public void fecharConexao() {
		try {
			em.close();
			emf.close();
			System.out.println("Desconectou do JPA!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
