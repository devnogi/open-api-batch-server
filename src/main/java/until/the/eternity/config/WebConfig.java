package until.the.eternity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.RealtimeSortField;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.common.enums.SortField;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, SortField.class, SortField::from);
        registry.addConverter(String.class, SortDirection.class, SortDirection::from);
        registry.addConverter(String.class, RealtimeSortField.class, RealtimeSortField::from);
    }
}
