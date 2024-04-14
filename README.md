# Rob� de Negocia��o de Criptomoedas na Binance

Este projeto � uma aplica��o em Java 17 que automatiza a compra e venda de criptomoedas na plataforma Binance. Ele permite aos usu�rios definirem estrat�gias personalizadas para negocia��o, aproveitando as oportunidades do mercado de criptomoedas.

## Pr�-requisitos

Antes de usar este projeto, certifique-se de ter os seguintes requisitos:

- Java 17 instalado no seu sistema.
- MySQL instalado para armazenar os detalhes das opera��es do rob�.
- Um arquivo `credenciais.xml` deve ser inclu�do no projeto, contendo as informa��es de autentica��o da Binance e outras credenciais necess�rias para o funcionamento correto do rob�.

## Estrutura do arquivo `credenciais.xml`

O arquivo `credenciais.xml` deve seguir a seguinte estrutura:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<credenciais>
    <KUCOIN_API_KEY>sua chave da kucoin</KUCOIN_API_KEY>
    <KUCOIN_SECRET_KEY>sua chave da kucoin</KUCOIN_SECRET_KEY>
    <KUCOIN_PASS_PHRASE>sua chave da kucoin</KUCOIN_PASS_PHRASE>
    <BINANCE_API_KEY>sua chave da binance</BINANCE_API_KEY>
    <BINANCE_SECRET_KEY>sua chave da binance</BINANCE_SECRET_KEY>
    <GMAIL_USER>seu email</GMAIL_USER>
    <GMAIL_PASSWORD>seu email</GMAIL_PASSWORD>
    <EMAIL_DESTINO>email para envio das informacoes</EMAIL_DESTINO>
    <DISCORD_TOKEN>informacoes discord</DISCORD_TOKEN>
    <DISCORD_PERMISSION_INT>8</DISCORD_PERMISSION_INT>
    <DISCORD_CHANNEL_ID>canal do discord</DISCORD_CHANNEL_ID>
    <TELEGRAN_GROUP_ID>id grupo do telegran</TELEGRAN_GROUP_ID>
    <TELEGRAM_TOKEN>token telegran</TELEGRAM_TOKEN>
    <DEBUG>True</DEBUG>
</credenciais>
</xml>

```

## Configura��o

### 1. Credenciais da Binance

Antes de come�ar, � necess�rio gerar as credenciais de acesso na plataforma Binance. Ap�s obt�-las, inclua-as no arquivo `credenciais.xml` conforme a estrutura especificada na se��o anterior.

### 2. Banco de Dados MySQL

� necess�rio configurar um banco de dados MySQL para armazenar os detalhes das opera��es do rob�. Siga os passos abaixo para configurar o banco de dados:

1. Acesse o MySQL e crie um novo banco de dados para o projeto.

2. Crie um novo usu�rio e senha para o banco de dados.

3. Conceda as permiss�es necess�rias para o novo usu�rio sobre o banco de dados criado.

4. Atualize as configura��es de conex�o com o banco de dados no arquivo do projeto, conforme necess�rio.

## Execu��o

### Compila��o via Maven

Antes de executar o rob�, � necess�rio compilar o projeto usando o Maven. 

Execu��o no Windows
Se o seu sistema operacional for Windows, voc� pode executar o rob� usando o arquivo executar-robo.bat. Este arquivo acessar� o Java via linha de comando e apontar� para o arquivo compilado do Maven. Execute o seguinte comando via linha de comando:

```bash
executar-robo.bat
```

Execu��o em outros sistemas operacionais
Para sistemas operacionais diferentes do Windows, voc� pode executar o arquivo compilado diretamente via linha de comando. Use o seguinte comando na raiz do projeto:

```bash
java -jar caminho/do/arquivo/compilado.jar
```

Certifique-se de substituir caminho/do/arquivo/compilado.jar pelo caminho correto do arquivo compilado do Maven.


## Licen�a
Este projeto � disponibilizado sob a licen�a MIT.

## Contato
Se voc� tiver d�vidas, sugest�es ou quiser contribuir para o projeto, sinta-se � vontade para entrar em contato atrav�s dos seguintes meios:

Email: wsystec.sistema@gmail.com
GitHub Issues: https://github.com/WesleySouzaSilva/projeto_robo_operacoes_binance

##Estado do Projeto
Atualmente, o projeto est� em desenvolvimento ativo. Novas funcionalidades est�o sendo adicionadas e melhorias est�o sendo feitas continuamente.

##Agradecimentos
Agradecemos a todos os contribuidores que ajudaram a tornar este projeto poss�vel.

##Autor
Este projeto foi desenvolvido por Wesley Souza.