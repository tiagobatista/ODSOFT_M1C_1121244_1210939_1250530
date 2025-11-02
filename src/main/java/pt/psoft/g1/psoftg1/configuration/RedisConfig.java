package pt.psoft.g1.psoftg1.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuração do Redis para cache da aplicação
 *
 * Esta configuração define como os dados são serializados/deserializados
 * quando salvos no Redis.
 *
 * - Keys: sempre strings (para facilitar busca e debug)
 * - Values: JSON (para suportar objetos complexos)
 * - Hash Keys: strings
 * - Hash Values: strings (simples e compatível)
 */
@Configuration
@ConditionalOnProperty(name = "repository.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * Configura o RedisTemplate para ser usado pela aplicação
     *
     * O RedisTemplate é a interface principal para interagir com Redis
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializador para keys (sempre String)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Serializador para values (JSON com type info)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Configurar serializadores
        // Keys são sempre strings
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Values podem ser JSON (para objetos complexos)
        template.setValueSerializer(jsonSerializer);

        // Para Hash values, usamos String por simplicidade
        template.setHashValueSerializer(stringSerializer);

        // Enable transaction support (opcional)
        template.setEnableTransactionSupport(false);

        template.afterPropertiesSet();

        return template;
    }
}