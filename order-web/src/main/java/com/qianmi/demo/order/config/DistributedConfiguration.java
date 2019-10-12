package com.qianmi.demo.order.config;

import com.qianmi.demo.order.CreateOrderSaga;
import com.rabbitmq.client.Channel;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.serialization.Serializer;
import org.slf4j.Logger;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;

import static org.slf4j.LoggerFactory.getLogger;

//@Profile("dev")
@EnableDiscoveryClient
@Configuration
public class DistributedConfiguration {

    // Example function providing a Spring Cloud Connector
    private static final Logger LOGGER = getLogger(DistributedConfiguration.class);

    @Value("${axon.amqp.exchange}")
    private String exchangeName;

    @Bean
    public Queue queue(){
        return new Queue("orderqueue", true);
    }

    @Bean
    public Exchange exchange(){
        return ExchangeBuilder.fanoutExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Binding queueBinding() {
        return BindingBuilder.bind(queue()).to(exchange()).with("").noargs();
    }


    @Bean
    public SpringAMQPMessageSource queueMessageSource(Serializer serializer){
        return new SpringAMQPMessageSource(serializer){
            @Override
            @RabbitListener(queues = "orderqueue")
            @Transactional
            public void onMessage(Message message, Channel channel) throws Exception {
                LOGGER.debug("Message received: "+message.toString());
                super.onMessage(message, channel);
            }
        };
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    //FIXME 还有哪些配置？
    // 运作机制和流程；举例子
    @Bean
    public SagaConfiguration<CreateOrderSaga> orderSagaConfiguration(Serializer serializer){
        //sagaConfiguration.registerHandlerInterceptor(c->transactionManagingInterceptor());
        return SagaConfiguration.subscribingSagaManager(CreateOrderSaga.class, c-> queueMessageSource(serializer));
    }


}
