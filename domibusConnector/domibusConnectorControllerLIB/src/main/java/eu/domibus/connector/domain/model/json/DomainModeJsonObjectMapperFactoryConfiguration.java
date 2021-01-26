package eu.domibus.connector.domain.model.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.domibus.connector.common.annotations.DomainModelJsonObjectMapper;
import eu.domibus.connector.domain.model.LargeFileReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainModeJsonObjectMapperFactoryConfiguration {

//    private static ObjectMapper OBJECT_MAPPER;
//
//    public static ObjectMapper getObjectMapper() {
//        if (OBJECT_MAPPER == null) {
//            OBJECT_MAPPER = new DomainModeJsonObjectMapperFactory().getMapper();
//        }
//        return OBJECT_MAPPER;
//    }

    @Bean(name = DomainModelJsonObjectMapper.VALUE)
    public ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule largeFileReferenceModule = new SimpleModule();
        largeFileReferenceModule.addSerializer(LargeFileReference.class, new LargeFileReferenceSerializer(LargeFileReference.class));
        largeFileReferenceModule.addDeserializer(LargeFileReference.class, new LargeFileDeserializer(LargeFileReference.class));

        mapper.registerModule(largeFileReferenceModule);
        return mapper;
    }

}