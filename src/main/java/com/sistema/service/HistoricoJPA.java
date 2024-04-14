package com.sistema.service;

import javax.persistence.EntityManager;

import com.sistema.controll.Principal;
import com.sistema.model.Historico;

public class HistoricoJPA {
	
	private ConexaoJPA conexaoJPA = Principal.getConexaoJPA();

	public HistoricoJPA(ConexaoJPA conexaoJPA) {
		this.conexaoJPA = conexaoJPA;

	}

	private EntityManager getEntityManager() {
		return conexaoJPA.getEntityManager();
	}

	public boolean inserir(Historico pojo) {
		try {
			getEntityManager().getTransaction().begin();
			getEntityManager().persist(pojo);
			getEntityManager().getTransaction().commit();
			System.out.println("Dados inseridos da operacao " + pojo.getTipo());
			return true;
		} catch (Exception e) {
			getEntityManager().getTransaction().rollback();
			System.out.println("Erro ao inserir dados empresa " + pojo.getTipo());
			e.printStackTrace();
			return false;
		}
	}

}
