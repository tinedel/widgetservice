package ua.kiev.tinedel.widget.widgetservice.utils;

import ua.kiev.tinedel.widget.widgetservice.models.Widget;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class DataGenerator {
    public static Widget buildWidget(UUID id) {
        return Widget.builder()
                .height(10)
                .width(10)
                .x(0)
                .y(0)
                .lastModifiedDate(Instant.now().minus(10, ChronoUnit.SECONDS))
                .zIndex(0)
                .id(id)
                .build();
    }
}
