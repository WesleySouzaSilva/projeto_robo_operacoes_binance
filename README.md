# Robô de Negociação de Criptomoedas na Binance

Este projeto é uma aplicação em Java 17 que automatiza a compra e venda de criptomoedas na plataforma Binance. Ele permite aos usuários definirem estratégias personalizadas para negociação, aproveitando as oportunidades do mercado de criptomoedas.

## Pré-requisitos

Antes de usar este projeto, certifique-se de ter os seguintes requisitos:

- Java 17 instalado no seu sistema.
- MySQL instalado para armazenar os detalhes das operações do robô.
- Um arquivo `credenciais.xml` deve ser incluído no projeto, contendo as informações de autenticação da Binance e outras credenciais necessárias para o funcionamento correto do robô.

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

## Configuração

### 1. Credenciais da Binance

Antes de começar, é necessário gerar as credenciais de acesso na plataforma Binance. Após obtê-las, inclua-as no arquivo `credenciais.xml` conforme a estrutura especificada na seção anterior.

### 2. Banco de Dados MySQL

É necessário configurar um banco de dados MySQL para armazenar os detalhes das operações do robô. Siga os passos abaixo para configurar o banco de dados:

1. Acesse o MySQL e crie um novo banco de dados para o projeto.

2. Crie um novo usuário e senha para o banco de dados.

3. Conceda as permissões necessárias para o novo usuário sobre o banco de dados criado.

4. Atualize as configurações de conexão com o banco de dados no arquivo do projeto, conforme necessário.

## Execução

### Compilação via Maven

Antes de executar o robô, é necessário compilar o projeto usando o Maven. 

Execução no Windows
Se o seu sistema operacional for Windows, você pode executar o robô usando o arquivo executar-robo.bat. Este arquivo acessará o Java via linha de comando e apontará para o arquivo compilado do Maven. Execute o seguinte comando via linha de comando:

```bash
executar-robo.bat
```

Execução em outros sistemas operacionais
Para sistemas operacionais diferentes do Windows, você pode executar o arquivo compilado diretamente via linha de comando. Use o seguinte comando na raiz do projeto:

```bash
java -jar caminho/do/arquivo/compilado.jar
```

Certifique-se de substituir caminho/do/arquivo/compilado.jar pelo caminho correto do arquivo compilado do Maven.


## Licença
Este projeto é disponibilizado sob a licença MIT.

## Contato
Se você tiver dúvidas, sugestões ou quiser contribuir para o projeto, sinta-se à vontade para entrar em contato através dos seguintes meios:

Email: wsystec.sistema@gmail.com

GitHub Issues: https://github.com/WesleySouzaSilva/projeto_robo_operacoes_binance

## Estado do Projeto
Atualmente, o projeto está em desenvolvimento ativo. Novas funcionalidades estão sendo adicionadas e melhorias estão sendo feitas continuamente.

## Agradecimentos
Agradecemos a todos os contribuidores que ajudaram a tornar este projeto possível.

## Autor
Este projeto foi desenvolvido por Wesley Souza.

## Termos de Uso
Este projeto esta aberto para a comunidade, sendo assim o autor não se responsabiliza por perdas ou ganho, de qualquer natureza, envolvendo esse codigo. Cada ajuste e implementação, fica de responsabilidade de quem esta usando o projeto.
