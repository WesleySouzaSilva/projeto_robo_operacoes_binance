package com.sistema.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.sistema.controll.Principal;
import com.sistema.model.Operacao;

public class OperacaoJPA {

	private ConexaoJPA conexaoJPA = Principal.getConexaoJPA();

	public OperacaoJPA(ConexaoJPA conexaoJPA) {
		this.conexaoJPA = conexaoJPA;

	}

	private EntityManager getEntityManager() {
		return conexaoJPA.getEntityManager();
	}

	public boolean inserir(Operacao pojo) {
		try {
			getEntityManager().getTransaction().begin();
			getEntityManager().persist(pojo);
			getEntityManager().getTransaction().commit();
			System.out.println("Dados inseridos da operacao " + pojo.getTipoOperacao());
			return true;
		} catch (Exception e) {
			getEntityManager().getTransaction().rollback();
			System.out.println("Erro ao inserir dados empresa " + pojo.getTipoOperacao());
			e.printStackTrace();
			return false;
		}
	}

	public boolean getMoedaCompradaNoDia(String moeda, Date dataAtual) {
		String sql = "SELECT COUNT(*) FROM operacao WHERE moeda = :moeda AND tipo_operacao = 'COMPRA' AND DATE(data_operacao) = :data";
		int count = 0;

		try {
			getEntityManager().getTransaction().begin();
			Query query = getEntityManager().createNativeQuery(sql);
			query.setParameter("moeda", moeda);
			query.setParameter("data", dataAtual);

			count = ((Number) query.getSingleResult()).intValue();
			getEntityManager().getTransaction().commit();
		} catch (Exception e) {
			getEntityManager().getTransaction().rollback();
			e.printStackTrace();
		}

		return count > 0;
	}
	
	public BigDecimal getValorCompraAtivo(String moeda, String campo) {
        BigDecimal valor = BigDecimal.ZERO;
        String sql = "SELECT COALESCE(o." + campo + ", 0) FROM Operacao o WHERE o.moeda = :moeda AND o.ativo = 'SIM' AND o.tipoOperacao = 'COMPRA'";

        try {
            getEntityManager().getTransaction().begin();
            Query query = getEntityManager().createQuery(sql);
            query.setParameter("moeda", moeda);

            Object result = query.getSingleResult();
            if (result != null) {
                valor = (BigDecimal) result;
            }
            getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            getEntityManager().getTransaction().rollback();
            e.printStackTrace();
        }
        return valor;
    }
	
	public boolean atualizarValor(String moeda, String campo, BigDecimal valor) {
        EntityTransaction transaction = null;
        try {
            EntityManager entityManager = getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();

            String jpql = "UPDATE Operacao o SET o." + campo + " = :valor WHERE o.moeda = :moeda AND o.tipoOperacao = 'COMPRA' AND o.ativo = 'SIM'";
            int updatedEntities = entityManager.createQuery(jpql)
                    .setParameter("valor", valor)
                    .setParameter("moeda", moeda)
                    .executeUpdate();

            transaction.commit();
            return updatedEntities > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
	
	public String getDataCompra(String moeda) {
        String valor = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String jpql = "SELECT o.dataOperacao FROM Operacao o WHERE o.moeda = :moeda AND o.ativo = 'SIM' AND o.tipoOperacao = 'COMPRA'";

        try {
            Query query = getEntityManager().createQuery(jpql);
            query.setParameter("moeda", moeda);

            Date dataOperacao = (Date) query.getSingleResult();
            if (dataOperacao != null) {
                valor = dateFormat.format(dataOperacao);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valor;
    }
	
	public boolean atualizarStatusAtivo(String moeda, String status) {
        EntityTransaction transaction = null;
        try {
            EntityManager entityManager = getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();

            String jpql = "UPDATE Operacao o SET o.ativo = :status WHERE o.moeda = :moeda AND o.tipoOperacao = 'COMPRA'";
            int updatedEntities = entityManager.createQuery(jpql)
                    .setParameter("status", status)
                    .setParameter("moeda", moeda)
                    .executeUpdate();

            transaction.commit();
            return updatedEntities > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
}
