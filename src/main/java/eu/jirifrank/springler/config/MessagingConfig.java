package eu.jirifrank.springler.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
//
//    @Value("${mq.measurements.name}")
//    private String messarurementsExchangeName;
//
//    @Bean
//    Queue queue() {
//        return new Queue(messarurementsExchangeName, false);
//    }
//
//    @Bean
//    FanoutExchange exchange() {
//        return new FanoutExchange(messarurementsExchangeName);
//    }
//
//    @Bean
//    Binding binding(Queue queue, FanoutExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange);
//    }
//
//    @Bean
//    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames(messarurementsExchangeName);
////        container.setMessageListener(listenerAdapter);
//        return container;
//    }
//
////    @Bean
////    MessageListenerAdapter listenerAdapter(Receiver receiver) {
////        return new MessageListenerAdapter(receiver, "receiveMessage");
////    }
}
