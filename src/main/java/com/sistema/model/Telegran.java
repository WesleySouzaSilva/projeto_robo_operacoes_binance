package com.sistema.model;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Telegran {
	
    public  void enviarMensagegm(String token, String grupo, String mensagem) {
        try {
            URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String postData = "chat_id=" + grupo + "&text=" + mensagem;
            connection.getOutputStream().write(postData.getBytes("UTF-8"));

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Erro no sendMessage, código de status: " + responseCode);
            }else if(responseCode == 200){
            	System.out.println("Mensagem enviada, código de status: " + responseCode);
            }
        } catch (IOException e) {
            System.out.println("Erro no sendMessage: " + e.getMessage());
        }
    }

}
