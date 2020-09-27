package ua.kiev.tinedel.widget.widgetservice.models;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@SuppressWarnings("DefaultAnnotationParam")
@FieldDefaults(makeFinal = false, level = AccessLevel.PRIVATE)
@Data
@Builder
public class Widget {

    @Builder.Default
    UUID id = UUID.randomUUID();

    int x;
    int y;
    int z;

    int width;
    int height;
}
