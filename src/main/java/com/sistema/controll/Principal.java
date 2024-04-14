package com.sistema.controll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.sistema.model.BinanceAPI;
import com.sistema.model.Credenciais;
import com.sistema.model.Historico;
import com.sistema.model.MoedaInfo;
import com.sistema.service.ConexaoJPA;
import com.sistema.service.HistoricoJPA;
import com.sistema.service.OperacaoJPA;

public class Principal {

	private static ConexaoJPA conexaoJPA = null;
	private static OperacaoJPA operacaoJPA = null;
	private static HistoricoJPA historicoJPA = null;

	private static Credenciais credenciais = new Credenciais();
	private static BinanceAPI binanceAPI = new BinanceAPI(credenciais.getBinanceApiKey(),
			credenciais.getBinanceSecretKey());
	private static List<MoedaInfo> listaMoedas = new ArrayList<>();

	public static void main(String[] args) {
		try {
			conexaoJPA = new ConexaoJPA("persistenceJPA");
			operacaoJPA = new OperacaoJPA(conexaoJPA);
			historicoJPA = new HistoricoJPA(conexaoJPA);

			boolean sucesso = historicoJPA
					.inserir(new Historico(null, "INICIO ROBO", "inicio analise robo", dataAtualTimeStamp()));

			// listaMoedas = operacaoDAO.listarTodosMoedasCompradas();
			System.out.println("sucesso inico robo : " + sucesso);
			System.out.println("----- INICIANDO ROBO DE OPERACOES -----");

			listaMoedas = binanceAPI.verificaMoedas();
			binanceAPI.operarComMoedas(listaMoedas);
//        	binanceAPI.getSaldoCarteira("USDT");
//        	binanceAPI.comprarCripto("TIAUSDT", BigDecimal.TEN);
			System.out.println("----- CARREGOU LISTA DE MOEDAS -----");
			System.out.println("print dados : " + listaMoedas.toString());
			System.out.println("print qtde de moedas encontradas : " + listaMoedas.size());
			System.out.println("----- INICIANDO VERIFICACAO COMPRA OU VENDA -----");
			System.out.println("----- FINALIZOU ROBO DE OPERACOES -----");
			System.out.println("\n");
			boolean suce = historicoJPA
					.inserir(new Historico(null, "FINALIZOU ROBO", "finalizou analise robo", dataAtualTimeStamp()));
			System.out.println("sucesso finalizou robo : " + suce);
		} finally {
			if (conexaoJPA != null) {
				conexaoJPA.fecharConexao();
			}
		}
		System.exit(0);

	}

	private static LocalDateTime dataAtualTimeStamp() {
		LocalDateTime dataAtual = LocalDateTime.now();
		DateTimeFormatter formatar = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String dataFormatada = dataAtual.format(formatar);
		LocalDateTime data = LocalDateTime.parse(dataFormatada, formatar);
		return data;
	}

	public static ConexaoJPA getConexaoJPA() {
		return conexaoJPA;
	}

	public static OperacaoJPA getOperacaoJPA() {
		return operacaoJPA;
	}

	public static HistoricoJPA getHistoricoJPA() {
		return historicoJPA;
	}

}
