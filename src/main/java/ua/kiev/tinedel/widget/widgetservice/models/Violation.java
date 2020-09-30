package ua.kiev.tinedel.widget.widgetservice.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Violation {
    String fieldName;
    String message;
}
