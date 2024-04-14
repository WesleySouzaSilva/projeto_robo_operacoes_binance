package com.sistema.model.util;

import java.math.BigDecimal;

public enum ParametrosMoeda {
	
	PORCENTAGEM_RENDIMENTO_POSITIVO(new BigDecimal("5")),
	PORCENTAGEM_NEGATIVA(new BigDecimal("2")),
    PORCENTAGEM_POSITIVA(new BigDecimal("3")),
    VENDA_LUCRO(new BigDecimal("60"));

    private BigDecimal valor;

    private ParametrosMoeda(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValor() {
        return valor;
    }

}
