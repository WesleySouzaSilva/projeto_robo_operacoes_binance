package com.sistema.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "operacao")
public class Operacao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "moeda", nullable = false, length = 100)
	private String moeda;
	
	@Column(name = "tipo_operacao", nullable = false, length = 100)
	private String tipoOperacao;
	
	@Column(name = "quantidade_moeda", nullable = false, columnDefinition = "DECIMAL(10,5)")
	private BigDecimal quantidadeMoeda;
	
	@Column(name = "preco_moeda", nullable = false, columnDefinition = "DECIMAL(10,5)")
	private BigDecimal precoMoeda;
	
	@Column(name = "valor_investido", nullable = false, columnDefinition = "DECIMAL(10,5)")
	private BigDecimal valorInvestido;
	
	@Column(name = "lucro", nullable = false, columnDefinition = "DECIMAL(10,5)")
	private BigDecimal lucro;
	
	@Column(name = "valor_ajuste", nullable = false, columnDefinition = "DECIMAL(10,5)")
	private BigDecimal valorAjuste;

	@Column(name = "valor_taxa", nullable = false, columnDefinition = "DECIMAL(10,5)")
	private BigDecimal valorTaxa;
	
	@Column(name = "data_operacao", columnDefinition="DATETIME")
	private LocalDateTime  dataOperacao;
	
	@Column(name = "ativo", nullable = false, length = 3)
	private String ativo;


}
