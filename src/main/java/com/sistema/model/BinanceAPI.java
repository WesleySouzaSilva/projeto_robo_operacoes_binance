package com.sistema.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.domain.market.TickerStatistics;
import com.sistema.controll.Principal;
import com.sistema.model.util.ParametrosMoeda;
import com.sistema.service.HistoricoJPA;
import com.sistema.service.OperacaoJPA;


public class BinanceAPI {
	
	private static final String CAMINHO_LOG = "C:\\Log Robo Operacoes\\log-operacoes.txt";
	private BigDecimal porcentagemNegativa = ParametrosMoeda.PORCENTAGEM_NEGATIVA.getValor();
	private BigDecimal porcentagemPositiva = ParametrosMoeda.PORCENTAGEM_POSITIVA.getValor();
	private BigDecimal vendaLucro = ParametrosMoeda.VENDA_LUCRO.getValor();
	private Telegran telegran = new Telegran();
	private Credenciais credenciais = new Credenciais();
	private static BigDecimal novoInicioMoeda = BigDecimal.ZERO;
	private List<BigDecimal> mediasMoveis7 = new ArrayList<>();
	private List<BigDecimal> mediasMoveis25 = new ArrayList<>();
	private List<BigDecimal> mediasMoveis99 = new ArrayList<>();
	private OperacaoJPA operacaoJPA = Principal.getOperacaoJPA();
	private HistoricoJPA historicoJPA = Principal.getHistoricoJPA();

	private final BinanceApiRestClient binanceApiRestClient;

	public BinanceAPI(String apiKey, String secretKey) {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
		binanceApiRestClient = factory.newRestClient();
	}

	private long getInicioDia() {
		// Obter a data atual
		long currentTimeMillis = System.currentTimeMillis();

		// Calcular o início do dia em milissegundos (00:00:00)
		return currentTimeMillis - (currentTimeMillis % (24 * 60 * 60 * 1000));
	}

	public List<MoedaInfo> verificaMoedas() {
		List<MoedaInfo> moedasInteressantes = new ArrayList<>();

		// Obter moedas na carteira
		List<String> moedasNaCarteira = obterMoedasNaCarteira();
		System.out.println("moedas na carteira : " + moedasNaCarteira);

		try {
			ExchangeInfo exchangeInfo = binanceApiRestClient.getExchangeInfo();
			if (exchangeInfo != null) {
				System.out.println("ExchangeInfo recuperado com sucesso.");
			} else {
				System.out.println("Falha ao recuperar ExchangeInfo.");
			}

			long startOfDay = getInicioDia();

			List<TickerStatistics> tickers = binanceApiRestClient.getAll24HrPriceStatistics();

			for (TickerStatistics ticker : tickers) {
				String symbol = ticker.getSymbol();

				// Verificar se o símbolo termina com "USDT"
				if (symbol.endsWith("USDT")) {
					// Verificar se a moeda já está na lista de moedas da carteira
					if (moedasNaCarteira.contains(symbol)) {
						System.out.println("Moeda " + symbol + " já está na carteira. Ignorando.");
						continue;
					}

					// Obter dados históricos da moeda desde a abertura do dia até o momento
					List<Candlestick> candlesticks = binanceApiRestClient.getCandlestickBars(symbol,
							CandlestickInterval.FIFTEEN_MINUTES, null, startOfDay, System.currentTimeMillis());

					// Calcular a variação percentual em relação ao valor de abertura
					if (!candlesticks.isEmpty()) {
						Candlestick firstCandle = candlesticks.get(0);
						Candlestick lastCandle = candlesticks.get(candlesticks.size() - 1);

						BigDecimal openPrice = new BigDecimal(firstCandle.getOpen());
						BigDecimal currentPrice = new BigDecimal(lastCandle.getClose());

						BigDecimal priceChangePercent = currentPrice.subtract(openPrice)
								.divide(openPrice, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));

						System.out.println("Variação percentual para " + symbol + ": " + priceChangePercent + "%");

						// Verificar se a variação é positiva e pelo menos -igual a ParametrosMoeda.PORCENTAGEM_RENDIMENTO_POSITIVO
						if (priceChangePercent.compareTo(ParametrosMoeda.PORCENTAGEM_RENDIMENTO_POSITIVO.getValor()) > 0) {
							BigDecimal volume = new BigDecimal(ticker.getVolume());
							MoedaInfo moedaInfo = new MoedaInfo(symbol, priceChangePercent, volume);
							moedasInteressantes.add(moedaInfo);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(moedasInteressantes, Comparator.comparing(MoedaInfo::getVolume).reversed());
		return moedasInteressantes;
	}

	public List<String> obterMoedasNaCarteira() {
		List<String> moedasNaCarteira = new ArrayList<>();

		try {
			ExchangeInfo exchangeInfo = binanceApiRestClient.getExchangeInfo();
			if (exchangeInfo != null) {
				System.out.println("ExchangeInfo recuperado com sucesso.");
			} else {
				System.out.println("Falha ao recuperar ExchangeInfo.");
				return moedasNaCarteira; // Retorna lista vazia em caso de falha
			}

			long serverTime = binanceApiRestClient.getServerTime();
			long currentTime = System.currentTimeMillis();
			ZonedDateTime serverDateTime = Instant.ofEpochMilli(serverTime).atZone(ZoneId.of("UTC"));
			ZonedDateTime currentDateTime = Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault());

			long timeDifference = ChronoUnit.MILLIS.between(currentDateTime, serverDateTime);
			long adjustedTime = currentTime + timeDifference;

			Account account = binanceApiRestClient.getAccount(5000L, adjustedTime);
			List<AssetBalance> assetBalances = account.getBalances();

			for (AssetBalance assetBalance : assetBalances) {
				BigDecimal quantidade = new BigDecimal(assetBalance.getFree());
				if (quantidade.compareTo(BigDecimal.ZERO) > 0) {
					String moeda = assetBalance.getAsset() + "USDT";
					if (!moeda.equals("USDTUSDT")) {
						moedasNaCarteira.add(moeda);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return moedasNaCarteira;
	}

	public void operarComMoedas(List<MoedaInfo> moedasComprar) {
		List<String> moedasCarteira = obterMoedasNaCarteira();
		BigDecimal investimentoTotal = getSaldoCarteira("USDT");

		if (!moedasCarteira.isEmpty()) {
			for (String moedaLista : moedasCarteira) {
				List<Candlestick> candlesticks = obterDadosTempoReal(moedaLista, 20);

				System.out.println("Analisando moedas da carteira: " + moedaLista);

				if (!verificarRendimentoMoeda(moedaLista)) {
					// Atualiza o preço atual antes de realizar a venda
					BigDecimal precoAtual = new BigDecimal(candlesticks.get(candlesticks.size() - 1).getClose());

					BigDecimal qtdeMoedasEmCarteira = getSaldoCarteira(moedaLista.replace("USDT", ""));
					System.out.println("PEGOU A QTDE DE MOEDAS DO BANCO DE DADOS : " + qtdeMoedasEmCarteira);
					realizarVenda(moedaLista, qtdeMoedasEmCarteira, precoAtual);
				}
			}
		}

		if (investimentoTotal.compareTo(BigDecimal.ONE) <= 0) {
			System.out.println("Não possui saldo em carteira, não será efetuado operações");

		} else {

			BigDecimal saldoPorMoeda = BigDecimal.TEN;
			BigDecimal qtdeMoedasParaOperar = investimentoTotal.divide(saldoPorMoeda, 2, RoundingMode.HALF_UP);

			System.out.println("Investimento total: " + investimentoTotal);
			System.out.println("Quantidade de moedas para operar: " + qtdeMoedasParaOperar);

			for (MoedaInfo m : moedasComprar) {
				if (qtdeMoedasParaOperar.compareTo(BigDecimal.ZERO) <= 0) {
					System.out.println("Saldo insuficiente para comprar mais moedas.");
					break;
				}

				String moeda = m.getMoeda();
				List<Candlestick> candlesticks = obterDadosTempoReal(moeda, 20);

				System.out.println("Analisando moeda: " + moeda);
				System.out.println("Tamanho da lista de candlesticks: " + candlesticks.size());

				// Simplificando, calculando médias móveis e RSI manualmente
				BigDecimal sma7 = calcularMediaMovel(candlesticks, 7);
				BigDecimal sma25 = calcularMediaMovel(candlesticks, 25);
				BigDecimal sma99 = calcularMediaMovel(candlesticks, 99);
				BigDecimal rsi = calcularRSI(candlesticks, 6);

				System.out.println("SMA7: " + sma7);
				System.out.println("SMA25: " + sma25);
				System.out.println("SMA99: " + sma99);
				System.out.println("RSI: " + rsi + "\n");

				boolean tendenciaPreco = verificarTendenciaDePreco(sma7, sma25, sma99, candlesticks);
				boolean tendenciaVolume = verificarTendenciaDeVolume(candlesticks);
				boolean tendenciaRSI = verificarTendenciaDeRSI(rsi);

				novoInicioMoeda = atualizarNovoInicioMoeda(moeda, candlesticks);
				System.out.println("");

				if (moedasCarteira.contains(moeda)) {
					if (!verificarRendimentoMoeda(moeda)) {
						venderMoeda(moeda, candlesticks, tendenciaPreco, tendenciaVolume, tendenciaRSI);
						qtdeMoedasParaOperar = qtdeMoedasParaOperar.add(BigDecimal.ONE);
					}
				} else {
					if (tendenciaPreco && tendenciaRSI && tendenciaVolume) {
						System.out.println("SALDO PARA COMPRAR MOEDA " + moeda + ": " + saldoPorMoeda);
						if (saldoPorMoeda.compareTo(BigDecimal.TEN) >= 0
								&& getSaldoCarteira("USDT").compareTo(saldoPorMoeda) >= 0) {
							Date dataAtual = new Date();
							boolean comprouMoedaHoje = operacaoJPA.getMoedaCompradaNoDia(moeda, dataAtual);
							if (!comprouMoedaHoje) {
								comprarMoeda(moeda, saldoPorMoeda, candlesticks);
							} else {
								System.out.println(
										"Ja comprou hoje a moeda " + moeda + ". Não iremos comprar novamente!");
							}
						}
						qtdeMoedasParaOperar = qtdeMoedasParaOperar.subtract(BigDecimal.ONE);
					} else {
						System.out.println("Não passou nos parâmetros de tendência, então não será comprada a moeda");
					}
				}
			}

		}

	}

	private BigDecimal calcularMediaMovel(List<Candlestick> candlesticks, int periodo) {
		int tamanhoLista = candlesticks.size();

		if (tamanhoLista < periodo) {
			System.out.println("Ajustando o período para a quantidade disponível de candlesticks: " + tamanhoLista);
			periodo = tamanhoLista;
		}

		BigDecimal soma = BigDecimal.ZERO;
		for (int i = tamanhoLista - periodo; i < tamanhoLista; i++) {
			soma = soma.add(new BigDecimal(candlesticks.get(i).getClose()));
		}

		BigDecimal mediaMovel = soma.divide(new BigDecimal(periodo), 4, RoundingMode.HALF_UP);

		// Adicionar a média móvel à lista correspondente
		if (periodo == 7) {
			mediasMoveis7.add(mediaMovel);
		} else if (periodo == 25) {
			mediasMoveis25.add(mediaMovel);
		} else if (periodo == 99) {
			mediasMoveis99.add(mediaMovel);
		}

		return mediaMovel;
	}

	private BigDecimal calcularRSI(List<Candlestick> candlesticks, int periodo) {
		int tamanho = candlesticks.size();

		if (tamanho < periodo) {
			return BigDecimal.ZERO;
		}

		BigDecimal ganhoTotal = BigDecimal.ZERO;
		BigDecimal perdaTotal = BigDecimal.ZERO;

		// Calcular ganhos e perdas médias
		for (int i = 1; i < tamanho; i++) {
			BigDecimal diferenca = new BigDecimal(candlesticks.get(i).getClose())
					.subtract(new BigDecimal(candlesticks.get(i - 1).getClose()));

			if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
				ganhoTotal = ganhoTotal.add(diferenca);
			} else {
				perdaTotal = perdaTotal.add(diferenca.abs());
			}
		}

		// Calcular médias
		BigDecimal mediaGanho = ganhoTotal.divide(new BigDecimal(periodo), 4, RoundingMode.HALF_UP);
		BigDecimal mediaPerda = perdaTotal.divide(new BigDecimal(periodo), 4, RoundingMode.HALF_UP);

		if (mediaPerda.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.valueOf(100);
		}

		// Calcular o RSI
		BigDecimal RS = mediaGanho.divide(mediaPerda, 4, RoundingMode.HALF_UP);
		return BigDecimal.valueOf(100)
				.subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(RS), 4, RoundingMode.HALF_UP));
	}

	public boolean verificarRendimentoMoeda(String coin) {
		boolean retorno = true;
		BigDecimal valorCompra = operacaoJPA.getValorCompraAtivo(coin, "preco_moeda");
		BigDecimal valorAjuste = operacaoJPA.getValorCompraAtivo(coin, "valor_ajuste");
		BigDecimal precoAtual = obterPrecoAtualDaBinance(coin);
		BigDecimal valorNovoMinimo = valorAjuste.subtract(porcentagemNegativa);
		
		if (valorCompra.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal rendimento = precoAtual.subtract(valorCompra);
			BigDecimal porcentagemRendimento = rendimento.divide(valorCompra, 2, RoundingMode.HALF_UP)
					.multiply(new BigDecimal("100"));

			if (valorAjuste.compareTo(BigDecimal.ZERO) >= 0) {

				if (porcentagemRendimento.compareTo(porcentagemPositiva) >= 0) {
					boolean sucessoAtualizar = operacaoJPA.atualizarValor(coin, "valor_ajuste", porcentagemRendimento);
					System.out.println("sucesso atualizar valor rendimento : " + sucessoAtualizar);
					imprimirMensagem(
							"Moeda " + coin + " atingiu o rendimento base de : " + porcentagemRendimento + "%");
					retorno = true;
				}

				if (valorNovoMinimo.compareTo(porcentagemRendimento) <= 0
						&& valorAjuste.compareTo(BigDecimal.ZERO) > 0) {
					// Moeda caiu abaixo do novo mínimo, imprime mensagem e retorna false
					imprimirMensagem("Moeda " + coin + " caiu " + porcentagemNegativa + "% do rendimento atingido de "
							+ valorAjuste + "%.\nAtenção ao sinal de venda da moeda.");

					retorno = false;

				} else if (porcentagemRendimento.compareTo(valorAjuste) > 0) {
					// Moeda está rendendo mais que o valor ajuste, imprime mensagem e retorna true
					imprimirMensagem("Moeda " + coin + " está rendendo " + porcentagemRendimento
							+ "% desde a compra em " + operacaoJPA.getDataCompra(coin) + " \nPreço de compra : "
							+ valorCompra + "\nPreço Atual: " + precoAtual);
					retorno = true;
				}

				if (porcentagemRendimento.compareTo(BigDecimal.ZERO) < 0) {
					imprimirMensagem("Moeda " + coin + " caiu " + porcentagemRendimento
							+ "% do momento de compra.\nAtenção ao sinal de venda da moeda.");
					retorno = false;
				}


			} else {
				System.out.println("O valor ajuste e menor que 0");
			}
		}

		return retorno;
	}

	private BigDecimal obterPrecoAtualDaBinance(String coin) {
		try {
			String symbol = coin.endsWith("USDT") ? coin : coin + "USDT";
			TickerPrice ticker = binanceApiRestClient.getPrice(coin);

			if (ticker != null) {
				return new BigDecimal(ticker.getPrice());
			} else {
				System.out.println("Falha ao obter informações de preço para " + symbol);
				return BigDecimal.ZERO;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
	}

	private void imprimirMensagem(String mensagem) {
		escreverLog(mensagem);
		telegran.enviarMensagegm(credenciais.getTelegramToken(), credenciais.getTelegranGroupId(), mensagem);
	}

	private String formatarData(long timestamp) {
		Date dataAtual = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return sdf.format(dataAtual);
	}

	public BigDecimal getSaldoCarteira(String moedaCarteira) {
		// return new BigDecimal("200");
		long serverTime = binanceApiRestClient.getServerTime();
		long currentTime = System.currentTimeMillis();
		// Converte o tempo do servidor para um objeto ZonedDateTime
		ZonedDateTime serverDateTime = Instant.ofEpochMilli(serverTime).atZone(ZoneId.of("UTC"));
		ZonedDateTime currentDateTime = Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault());

		long timeDifference = ChronoUnit.MILLIS.between(currentDateTime, serverDateTime);
		long adjustedTime = currentTime + timeDifference;

		long recvWindow = 50000;

		Account account = binanceApiRestClient.getAccount(recvWindow, adjustedTime);
		AssetBalance usdtBalance = account.getAssetBalance(moedaCarteira);
		System.out.println("PEGOU SALDO DA CARTEIRA : " + usdtBalance.getFree());
		return new BigDecimal(usdtBalance.getFree());
	}

	public void comprarCripto(String criptoSymbol, BigDecimal quantidadeEmDolares) {
		// Obter o preço atual da criptomoeda em relação ao USDT
		TickerPrice tickerPriceCriptoUSDT = binanceApiRestClient.getPrice(criptoSymbol);
		BigDecimal precoCriptoUSDT = new BigDecimal(tickerPriceCriptoUSDT.getPrice());
		System.out.println("Preço atual de " + criptoSymbol + " em USDT: " + precoCriptoUSDT);

		// Obter o tamanho mínimo de uma ordem para o par de negociação
		BigDecimal tamanhoMinimo = getTamanhoMinimo(criptoSymbol);
		System.out.println("Tamanho mínimo para compra da moeda: " + tamanhoMinimo);

		// Ajustar a quantidade para ser maior ou igual ao tamanho mínimo
		BigDecimal quantidadeCriptoAjustada = ajustarQuantidade(criptoSymbol, quantidadeEmDolares, precoCriptoUSDT);
		System.out.println("Quantidade de " + criptoSymbol + " a ser comprada: " + quantidadeCriptoAjustada);

		// Obter o tempo do servidor da Binance
		long serverTime = binanceApiRestClient.getServerTime();

		System.out.println("Criando ordem para " + criptoSymbol + " com quantidade: " + quantidadeCriptoAjustada
				+ " e preço: " + precoCriptoUSDT);

		NewOrderResponse buyCriptoResponse = binanceApiRestClient.newOrder(
				NewOrder.marketBuy(criptoSymbol, quantidadeCriptoAjustada.toPlainString()).timestamp(serverTime));

		if (buyCriptoResponse != null) {
			System.out.println("Ordem de mercado para comprar " + criptoSymbol + " criada com sucesso.");

			// Obter o ID da ordem
			Long orderId = buyCriptoResponse.getOrderId();
			System.out.println("ID da ordem de compra de " + criptoSymbol + ": " + orderId);
		} else {
			System.out.println("Falha ao criar a ordem de mercado para comprar " + criptoSymbol + ".");
		}
	}

	private BigDecimal ajustarQuantidade(String symbol, BigDecimal quantidadeDesejadaEmDolares, BigDecimal precoAtual) {
		try {
			ExchangeInfo exchangeInfo = binanceApiRestClient.getExchangeInfo();
			SymbolInfo symbolInfo = exchangeInfo.getSymbolInfo(symbol);

			// Verificar se há informações disponíveis
			if (symbolInfo != null) {
				// Calcular a quantidade desejada baseada no preço atual e nos 10 dólares
				BigDecimal quantidadeDesejada = quantidadeDesejadaEmDolares.divide(precoAtual, 8, RoundingMode.DOWN);

				// Obter o passo da quantidade (LOT_SIZE)
				SymbolFilter lotSizeFilter = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);
				BigDecimal stepSize = new BigDecimal(lotSizeFilter.getStepSize());

				// Arredondar a quantidade para o passo mais próximo
//				BigDecimal quantidadeAjustada = quantidadeDesejada.divide(stepSize, 0, RoundingMode.HALF_UP)
//						.multiply(stepSize);
				BigDecimal quantidadeAjustada = quantidadeDesejada.divide(stepSize, 0, RoundingMode.DOWN)
						.multiply(stepSize);

				// Garantir que a quantidade ajustada atenda ao tamanho mínimo
				BigDecimal tamanhoMinimoOrdem = new BigDecimal(lotSizeFilter.getMinQty()).multiply(precoAtual);
				quantidadeAjustada = quantidadeAjustada.max(tamanhoMinimoOrdem.divide(precoAtual, 8, RoundingMode.UP));

				// Obter o filtro NOTIONAL
				SymbolFilter notionalFilter = symbolInfo.getSymbolFilter(FilterType.NOTIONAL);
				BigDecimal minNotional = new BigDecimal(notionalFilter.getMinNotional());

				// Verificar se a quantidade ajustada atende ao filtro NOTIONAL
				BigDecimal valorTotalOrdem = quantidadeAjustada.multiply(precoAtual);
				if (valorTotalOrdem.compareTo(minNotional) < 0) {
					quantidadeAjustada = minNotional.divide(precoAtual, 8, RoundingMode.UP);
					quantidadeAjustada = quantidadeAjustada.multiply(stepSize);
				}

				// Imprimir o valor do tamanho mínimo (lot size) e do filtro NOTIONAL
				System.out.println("Valor do lot size para " + symbol + ": " + lotSizeFilter.getMinQty());
				System.out.println("Valor do filtro NOTIONAL para " + symbol + ": " + notionalFilter.getMinNotional());

				return quantidadeAjustada;
			} else {
				System.out.println("Não foi possível obter informações do símbolo " + symbol);
				return BigDecimal.ZERO; 
			}
		} catch (Exception e) {
			e.printStackTrace();
			return BigDecimal.ZERO; 
		}
	}

	private BigDecimal getTamanhoMinimo(String symbol) {
		SymbolInfo symbolInfo = binanceApiRestClient.getExchangeInfo().getSymbolInfo(symbol);
		SymbolFilter lotSizeFilter = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);

		// Obtém o tamanho mínimo do lote
		return new BigDecimal(lotSizeFilter.getMinQty());
	}

	private List<Candlestick> obterDadosTempoReal(String moeda, int periodos) {

		String symbol = moeda.endsWith("USDT") ? moeda : moeda + "USDT";
		return binanceApiRestClient.getCandlestickBars(symbol, CandlestickInterval.FIFTEEN_MINUTES, periodos, null,
				null);
	}

	private boolean verificarTendenciaDePreco(BigDecimal sma7, BigDecimal sma25, BigDecimal sma99,
			List<Candlestick> candlesticks) {
		boolean tendenciaCompra = (sma7.compareTo(sma25) > 0 && sma7.compareTo(sma99) > 0);

		if (tendenciaCompra) {
			System.out.println("Tendência de Compra (SMA7 acima de SMA25 e SMA99 - Preço)");
		} else {
			System.out.println("Tendência de Venda (SMA7 abaixo de SMA25 e SMA99 - Preço)");

			// Verificar cruzamento
			if (verificarCruzamento(mediasMoveis7, mediasMoveis25)
					|| verificarCruzamento(mediasMoveis7, mediasMoveis99)) {
				System.out.println("Cruzamento de Médias Móveis 7 e 25 ou 7 e 99 detectado!");
				tendenciaCompra = true;
			}
		}

		return tendenciaCompra;
	}

	private boolean verificarTendenciaDeVolume(List<Candlestick> candlesticks) {
		BigDecimal ma5Volume = calcularMediaMovelVolume(candlesticks, 5);
		BigDecimal ma10Volume = calcularMediaMovelVolume(candlesticks, 10);

		boolean tendenciaCompra = (ma5Volume.compareTo(ma10Volume) < 0);
		if (tendenciaCompra) {
			System.out.println("Tendência de Compra (MA5 abaixo de MA10 - Volume)");
		} else {
			System.out.println("Tendência de Venda (MA5 acima de MA10 - Volume)");
		}

		return tendenciaCompra;
	}

	private boolean verificarTendenciaDeRSI(BigDecimal rsi) {
		boolean tendenciaCompra = (rsi.compareTo(new BigDecimal("45")) < 0);
		if (tendenciaCompra) {
			System.out.println("Tendência de Compra (RSI abaixo de 45)");
		} else if (rsi.compareTo(new BigDecimal("65")) > 0) {
			System.out.println("Tendência de Venda (RSI acima de 65)");
			tendenciaCompra = false;
		} else {
			System.out.println("Tendência Neutra (RSI entre 45 e 65)");
			tendenciaCompra = true;
		}

		return tendenciaCompra;
	}

//	private void imprimirDetalhesCandlesticks(List<Candlestick> candlesticks) {
//		System.out.println("Detalhes dos Candlesticks:");
//		for (Candlestick candlestick : candlesticks) {
//			System.out.println("Abertura: " + candlestick.getOpen() + " Fechamento: " + candlestick.getClose()
//					+ " Mínimo: " + candlestick.getLow() + " Máximo: " + candlestick.getHigh() + " Volume: "
//					+ candlestick.getVolume());
//		}
//	}

	public BigDecimal calcularMediaMovelVolume(List<Candlestick> candlesticks, int periodo) {
		int tamanhoLista = candlesticks.size();

		if (tamanhoLista < periodo) {
			throw new IllegalArgumentException(
					"Lista de candlesticks não tem tamanho suficiente para calcular a média móvel do volume.");
		}

		BigDecimal somaVolume = BigDecimal.ZERO;
		for (int i = tamanhoLista - periodo; i < tamanhoLista; i++) {
			somaVolume = somaVolume.add(new BigDecimal(candlesticks.get(i).getVolume()));
		}
		return somaVolume.divide(new BigDecimal(periodo), 4, RoundingMode.HALF_UP);
	}

	private BigDecimal atualizarNovoInicioMoeda(String moeda, List<Candlestick> candlesticks) {
	    BigDecimal novoInicio = BigDecimal.ZERO;

	    BigDecimal valorAjuste = operacaoJPA.getValorCompraAtivo(moeda, "valor_ajuste");

	    if (valorAjuste != null) {
	        if (valorAjuste.compareTo(novoInicio) > 0) {
	            novoInicio = new BigDecimal(candlesticks.get(candlesticks.size() - 1).getClose());
	            imprimirMensagem(
	                    "Definindo novoInicioMoeda para " + novoInicio + " USDT após compra da moeda " + moeda);
	            boolean sucesso = operacaoJPA.atualizarValor(moeda, "valor_ajuste", novoInicio);
	            System.out.println("valor de novo inico e maior que valor anterior, entao atualizou : " + sucesso);
	            boolean suce = historicoJPA.inserir(new Historico(null, "ATUALIZACAO",
	                    "atualizou valor novo inico meoda " + moeda + " para " + novoInicio + "", dataAtualTimeStamp()));
	            System.out.println("sucesso inserir historivo de atualizacao : " + suce);
	        }
	    }

	    return novoInicio;
	}


	private boolean verificarCruzamento(List<BigDecimal> maCurta, List<BigDecimal> maLonga) {
		int tamanhoCurta = maCurta.size();
		int tamanhoLonga = maLonga.size();

		if (tamanhoCurta > 1 && tamanhoLonga > 1) {
			BigDecimal ultimaCurta = maCurta.get(tamanhoCurta - 1);
			BigDecimal penultimaCurta = maCurta.get(tamanhoCurta - 2);
			BigDecimal ultimaLonga = maLonga.get(tamanhoLonga - 1);
			BigDecimal penultimaLonga = maLonga.get(tamanhoLonga - 2);

			// Cruzamento para cima
			if (penultimaCurta.compareTo(penultimaLonga) <= 0 && ultimaCurta.compareTo(ultimaLonga) > 0) {
				return true;
			}
			// Cruzamento para baixo
			else if (penultimaCurta.compareTo(penultimaLonga) >= 0 && ultimaCurta.compareTo(ultimaLonga) < 0) {
				return true;
			}
		}

		return false;
	}

	private void comprarMoeda(String criptoSymbol, BigDecimal saldoEmUSDT, List<Candlestick> candlesticks) {
		System.out.println("Condições de compra atendidas.");
		 

		BigDecimal precoAtual = new BigDecimal(candlesticks.get(candlesticks.size() - 1).getClose());

		// Verifica se há saldo suficiente para comprar a quantidade desejada
		BigDecimal quantidadeAjustada = ajustarQuantidade(criptoSymbol, saldoEmUSDT, precoAtual);
		if (quantidadeAjustada.compareTo(BigDecimal.ZERO) <= 0) {
			System.out.println("Saldo insuficiente para comprar a quantidade desejada.");
			return;
		}

		try {
			TickerPrice tickerPriceCriptoUSDT = binanceApiRestClient.getPrice(criptoSymbol);
			BigDecimal precoCriptoUSDT = new BigDecimal(tickerPriceCriptoUSDT.getPrice());

			// Obter o tempo do servidor da Binance
			long serverTime = binanceApiRestClient.getServerTime();

			System.out.println("Criando ordem para " + criptoSymbol + " com quantidade: " + quantidadeAjustada
					+ " e preço: " + precoCriptoUSDT);

			NewOrderResponse buyCriptoResponse = binanceApiRestClient.newOrder(
					NewOrder.marketBuy(criptoSymbol, quantidadeAjustada.toPlainString()).timestamp(serverTime));

			if (buyCriptoResponse != null) {
				System.out.println("Ordem de mercado para comprar " + criptoSymbol + " criada com sucesso.");

				// Obter o ID da ordem
				Long orderId = buyCriptoResponse.getOrderId();
				System.out.println("ID da ordem de compra de " + criptoSymbol + ": " + orderId);
			} else {
				System.out.println("Falha ao criar a ordem de mercado para comprar " + criptoSymbol + ".");
			}
			// Atualiza a quantidade em carteira
			BigDecimal valorCompra = precoAtual.multiply(quantidadeAjustada);
			BigDecimal calculaTaxa = valorCompra.multiply(new BigDecimal("0.001"));


			Operacao operacao = new Operacao(null, criptoSymbol, "COMPRA", quantidadeAjustada, precoAtual, saldoEmUSDT,
					BigDecimal.ZERO, BigDecimal.ZERO, calculaTaxa, dataAtualTimeStamp(), "SIM");
			boolean sucesso = operacaoJPA.inserir(operacao);
			System.out.println("Sucesso ao registrar no BD a compra da moeda " + criptoSymbol + ": " + sucesso);

			boolean sucessoHistorico = historicoJPA.inserir(new Historico(null,"COMPRA", "Comprou a moeda " + criptoSymbol
					+ ", valor moeda: " + precoAtual + ", quantidade: " + quantidadeAjustada, dataAtualTimeStamp()));
			System.out.println(
					"Sucesso ao inserir histórico da compra da moeda " + criptoSymbol + ": " + sucessoHistorico);

			imprimirMensagem("Compra de " + quantidadeAjustada + " " + criptoSymbol + " a " + precoAtual + " USDT às "
					+ formatarData(new Date().getTime()));
			System.out.println("Compra realizada com sucesso.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void venderMoeda(String criptoSymbol, List<Candlestick> candlesticks, boolean tendenciaPreco,
			boolean tendenciaVolume, boolean tendenciaRSI) {

		BigDecimal qtdeMoedasEmCarteira = getSaldoCarteira(criptoSymbol.replace("USDT", ""));
		System.out.println("qtde da moeda " + criptoSymbol + " em carteira " + qtdeMoedasEmCarteira);
		System.out.println("Possui a moeda em carteira. Verificando condições de venda.");
		BigDecimal valorAjuste = operacaoJPA.getValorCompraAtivo(criptoSymbol, "valor_ajuste");
		BigDecimal precoMoedaCompra = operacaoJPA.getValorCompraAtivo(criptoSymbol, "preco_moeda");

		BigDecimal precoAtualMoeda = new BigDecimal(binanceApiRestClient.getPrice(criptoSymbol).getPrice());

		// Calcular o valor inicial considerando o valor investido
		BigDecimal rendimento = BigDecimal.ZERO;
		if (precoAtualMoeda.compareTo(precoMoedaCompra) > 0) {
			rendimento = precoAtualMoeda.subtract(precoMoedaCompra).multiply(new BigDecimal(100));
		}

		// Calcular a porcentagem de rendimento
		System.out.println("Novo valor inicial da moeda: " + novoInicioMoeda);
		System.out.println("Porcentagem de rendimento: " + rendimento + "%");

		try {
	
			TickerPrice tickerPriceCriptoUSDT = binanceApiRestClient.getPrice(criptoSymbol);
			BigDecimal precoAtual = new BigDecimal(tickerPriceCriptoUSDT.getPrice());

			// Verificar condições para venda
			if (!verificarRendimentoMoeda(criptoSymbol)) {
				BigDecimal limiteNegativo = valorAjuste.subtract(porcentagemNegativa);
				System.out.println("valor limite negativo : " + limiteNegativo);
				if (rendimento.compareTo(limiteNegativo) <= 0
						|| rendimento.compareTo(vendaLucro) >= 0) {
					realizarVenda(criptoSymbol, qtdeMoedasEmCarteira, precoAtual);
				}
			} else {
				if (rendimento.compareTo(valorAjuste) >= 0) {
					// Atualizar novo início e realizar venda
					boolean atualizaNovoInicio = operacaoJPA.atualizarValor(criptoSymbol, "valor_ajuste", rendimento);
					System.out.println("Atualizou a porcentagem de lucro no BD: " + atualizaNovoInicio);
					imprimirMensagem("Atualização " + criptoSymbol + " de novoInicioMoeda para " + rendimento
							+ "%");
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void realizarVenda(String criptoSymbol, BigDecimal qtdeMoedasEmCarteira, BigDecimal precoAtual) {
		try {

			System.err.println("RECEBEU NA VENDA A MOEDA NAO FORMATADA : " + criptoSymbol);
			long serverTime = binanceApiRestClient.getServerTime();
			String moeda = criptoSymbol.replace("USDT", "");
			System.out.println("MOEDA SEM USDT : " + moeda);

			BigDecimal saldoDisponivel = getSaldoCarteira(moeda);
			System.out.println("Saldo disponível de " + criptoSymbol + ": " + saldoDisponivel);

			System.out.println("calculou a qtde em USDT para venda : " + precoAtual.multiply(qtdeMoedasEmCarteira));
			// Arredondar a quantidade para seguir o tamanho do passo
			BigDecimal quantidadeAjustada = ajustarQuantidade(criptoSymbol, precoAtual.multiply(qtdeMoedasEmCarteira),
					precoAtual);

			System.out.println("calculou a qtde em USDT para venda : " + qtdeMoedasEmCarteira);

			NewOrderResponse sellCriptoResponse = binanceApiRestClient.newOrder(
					NewOrder.marketSell(criptoSymbol, quantidadeAjustada.toPlainString()).timestamp(serverTime));

			if (sellCriptoResponse != null) {
				System.out.println("Ordem de mercado para vender " + moeda + " criada com sucesso.");

				// Obter o ID da ordem
				Long orderId = sellCriptoResponse.getOrderId();
				System.out.println("ID da ordem de venda de " + moeda + ": " + orderId);

				// Calcular o valor total da venda
				BigDecimal valorVenda = precoAtual.multiply(qtdeMoedasEmCarteira);

				// Calcular a taxa
				BigDecimal taxa = new BigDecimal("0.001");
				BigDecimal calculaTaxa = valorVenda.multiply(taxa);

				// Cria a operação de venda
				Operacao operacao = new Operacao(null, criptoSymbol, "VENDA", qtdeMoedasEmCarteira, precoAtual,
						valorVenda, BigDecimal.ZERO, BigDecimal.ZERO, calculaTaxa, dataAtualTimeStamp(),
						"SIM");

				boolean atualizaStatus = operacaoJPA.atualizarStatusAtivo(criptoSymbol, "NAO");
				System.out.println("atualizaou status moeda inativa : " + atualizaStatus);
				boolean sucessoVenda = operacaoJPA.inserir(operacao);
				System.out.println("Sucesso ao registrar no BD a venda da moeda " + criptoSymbol + ": " + sucessoVenda);

				// Insere o histórico
				boolean sucessoHistorico = historicoJPA.inserir(new Historico(null,"VENDA", "Vendeu a moeda " + criptoSymbol
						+ ", valor moeda: " + precoAtual + ", quantidade: " + qtdeMoedasEmCarteira, dataAtualTimeStamp()));
				System.out.println(
						"Sucesso ao inserir histórico da venda da moeda " + criptoSymbol + ": " + sucessoHistorico);

				// Imprime mensagem de venda realizada com sucesso
				imprimirMensagem("Venda de " + qtdeMoedasEmCarteira + " " + criptoSymbol + " a " + precoAtual
						+ " USDT às " + formatarData(new Date().getTime()));
				System.out.println("Venda realizada com sucesso.");
			} else {
				System.out.println("Falha ao criar a ordem de venda para " + criptoSymbol + ".");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void escreverLog(String mensagem) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(CAMINHO_LOG, true))) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dataFormatada = dateFormat.format(new Date());
			String log = "[" + dataFormatada + "] " + mensagem;
			writer.write(log);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private LocalDateTime dataAtualTimeStamp() {
		LocalDateTime dataAtual = LocalDateTime.now();
        DateTimeFormatter formatar = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String dataFormatada = dataAtual.format(formatar);
        LocalDateTime data = LocalDateTime.parse(dataFormatada, formatar);
        return data;
	}

}
