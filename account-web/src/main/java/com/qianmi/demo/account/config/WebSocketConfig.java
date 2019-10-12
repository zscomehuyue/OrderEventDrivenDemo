/*
 * Copyright (c) 2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qianmi.demo.account.config;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
//注解表明： 这个配置类不仅配置了 WebSocket，还配置了 基于代理的 STOMP 消息；
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    private Environment environment;
//  http://note.youdao.com/noteshare?id=b9fe1067f235542e2024c90ff87456f2
    //FIXME ???如何 使用 STOMP 代理来替换内存代理，代码如下：
//    对以上代码的分析（Analysis）：（干货——STOMP代理前缀和 应用程序前缀的意义）
//    A1）方法第一行启用了 STOMP 代理中继功能： 并将其目的地前缀设置为 "/topic" or "/queue" ；spring就能知道 所有目的地前缀为 "/topic" or "/queue" 的消息都会发送到 STOMP 代理中；
//    A2）方法第二行设置了 应用的前缀为 "app"：所有目的地以 "/app" 打头的消息（发送消息url not 连接url）都会路由到 带有 @MessageMapping
//    注解的方法中，而不会发布到 代理队列或主题中；
//            3）下图阐述了 代理中继如何 应用于 spring 的 STOMP 消息处理之中。与 上图的 关键区别在于： 这里不再模拟STOMP 代理的功能，而是由 代理中继将消息传送到一个 真正的消息代理来进行处理；
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if (ArrayUtils.contains(environment.getActiveProfiles(), "distributed-command-bus")) {
            //// 启用了 STOMP 代理中继功能，并将其代理目的地前缀设置为 /topic and /queue .
            config.enableStompBrokerRelay("/topic")
                    .setRelayHost("rabbitmq");
        } else {
            config.enableSimpleBroker("/topic");
        }
        config.setApplicationDestinationPrefixes("/app");
    }

    //FIXME withSockJS() 方法声明我们想要使用 SockJS 功能，如果WebSocket不可用的话，会使用 SockJS；
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .withSockJS();
    }
}
