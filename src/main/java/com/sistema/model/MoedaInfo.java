package com.sistema.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MoedaInfo {
	private String moeda;
	private BigDecimal porcentagemPreco;
	private BigDecimal volume;
	
	@Override
	public String toString() {
		return getMoeda();
	}
}
