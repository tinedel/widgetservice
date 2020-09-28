package ua.kiev.tinedel.widget.widgetservice.models;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("DefaultAnnotationParam")
@FieldDefaults(makeFinal = false, level = AccessLevel.PRIVATE)
@Data
@Accessors(chain = true)
@Builder
public class Widget {

    UUID id;

    @NotNull(message = "X coordinate is required")
    Integer x;
    @NotNull(message = "Y coordinate is required")
    Integer y;
    @NotNull(message = "Z-index is required")
    Integer zIndex;

    @Positive(message = "Width should be positive")
    int width;
    @Positive(message = "Height should be positive")
    int height;

    Instant lastModifiedDate;
}
