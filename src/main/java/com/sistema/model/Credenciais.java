package com.sistema.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.Getter;

@Getter
public class Credenciais {

	private String kucoinApiKey;
	private String kucoinSecretKey;
	private String kucoinPassPhrase;
	private String binanceApiKey;
	private String binanceSecretKey;
	private String gmailUser;
	private String gmailPassword;
	private String emailDestino;
	private String discordToken;
	private String discordPermissionInt;
	private String discordChannelId;
	private String telegranGroupId;
	private String telegramToken;
	private String debug;

	public Credenciais() {
	    carregarCredenciais("resources/crecredenciais.xml");
	}

	private void carregarCredenciais(String xmlFilePath) {
	    try {
	        File file = new File(xmlFilePath);
	        InputStream inputStream = new FileInputStream(file);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputStream);

			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("credenciais");

			for (int temp = 0; temp < nodeList.getLength(); temp++) {
				Node node = nodeList.item(temp);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					kucoinApiKey = element.getElementsByTagName("KUCOIN_API_KEY").item(0).getTextContent();
					kucoinSecretKey = element.getElementsByTagName("KUCOIN_SECRET_KEY").item(0).getTextContent();
					kucoinPassPhrase = element.getElementsByTagName("KUCOIN_PASS_PHRASE").item(0).getTextContent();
					binanceApiKey = element.getElementsByTagName("BINANCE_API_KEY").item(0).getTextContent();
					binanceSecretKey = element.getElementsByTagName("BINANCE_SECRET_KEY").item(0).getTextContent();
					gmailUser = element.getElementsByTagName("GMAIL_USER").item(0).getTextContent();
					gmailPassword = element.getElementsByTagName("GMAIL_PASSWORD").item(0).getTextContent();
					emailDestino = element.getElementsByTagName("EMAIL_DESTINO").item(0).getTextContent();
					discordToken = element.getElementsByTagName("DISCORD_TOKEN").item(0).getTextContent();
					discordPermissionInt = element.getElementsByTagName("DISCORD_PERMISSION_INT").item(0)
							.getTextContent();
					discordChannelId = element.getElementsByTagName("DISCORD_CHANNEL_ID").item(0).getTextContent();
					telegranGroupId = element.getElementsByTagName("TELEGRAN_GROUP_ID").item(0).getTextContent();
					telegramToken = element.getElementsByTagName("TELEGRAM_TOKEN").item(0).getTextContent();
					debug = element.getElementsByTagName("DEBUG").item(0).getTextContent();
				}
			}
	    } catch (FileNotFoundException e) {
	        System.out.println("Arquivo XML não encontrado: " + xmlFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
