# chatmp
Aplicação de chat (simples) em Android utilizando AMQP como Messaging Protocol. App utilizada para fins didáticos.

ATENÇÃO: para executar a aplicação aponte a mesma para um servidor RabbitMQ de sua preferência através do arquivo AMQPConnection.java (método setupConnectionFactory()).
O servidor RabbitMQ pode estar hospedado em sua própria máquina. Neste caso, utilize o seu endereço IP local (não utilizar o nome 'localhost' ou o endereço de loopback: 127.0.0.1).
