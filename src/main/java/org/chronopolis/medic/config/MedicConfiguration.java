package org.chronopolis.medic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import org.chronopolis.common.ace.AceConfiguration;
import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.OkBasicInterceptor;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.serializer.FulfillmentStrategyDeserializer;
import org.chronopolis.medic.client.serializer.FulfillmentStrategySerializer;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.support.PageDeserializer;
import org.chronopolis.rest.support.ZonedDateTimeDeserializer;
import org.chronopolis.rest.support.ZonedDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by shake on 2/20/17.
 */
@Configuration
@EnableConfigurationProperties({AceConfiguration.class, IngestConfiguration.class})
public class MedicConfiguration {
    private final Logger log = LoggerFactory.getLogger(MedicConfiguration.class);

    @Bean
    public Repairs repairs(IngestConfiguration ingest) {
        OkHttpClient okClient = new OkHttpClient.Builder()
                .addInterceptor(new OkBasicInterceptor(ingest.getUsername(), ingest.getPassword()))
                .build();

        // Not sure what types we'll actually need but we can figure that out over time
        Type bagPage = new TypeToken<Page<Bag>>() {}.getType();
        Type bagList = new TypeToken<List<Bag>>() {}.getType();

        Type repairPage = new TypeToken<Page<Repair>>() {}.getType();
        Type repairList = new TypeToken<List<Repair>>() {}.getType();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(bagPage, new PageDeserializer(bagList))
                .registerTypeAdapter(repairPage, new PageDeserializer(repairList))
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeDeserializer())
                .registerTypeAdapter(FulfillmentStrategy.class, new FulfillmentStrategySerializer())
                .registerTypeAdapter(FulfillmentStrategy.class, new FulfillmentStrategyDeserializer())
                .create();

        Retrofit r = new Retrofit.Builder()
                .client(okClient)
                .baseUrl(ingest.getEndpoint())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return r.create(Repairs.class);
    }

    @Bean
    public AceService ace(AceConfiguration configuration) {
        OkHttpClient okClient = new OkHttpClient.Builder()
                .addInterceptor(new OkBasicInterceptor(configuration.getUsername(), configuration.getPassword()))
                .build();

        Retrofit r = new Retrofit.Builder()
                .client(okClient)
                .baseUrl(configuration.getAm())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return r.create(AceService.class);
    }

    @Bean
    public TrackingThreadPoolExecutor<Repair> repairPool() {
        return new TrackingThreadPoolExecutor<>(8, 8, 15, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

}
