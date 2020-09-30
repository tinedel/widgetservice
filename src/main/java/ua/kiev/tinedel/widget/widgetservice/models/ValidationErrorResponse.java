package ua.kiev.tinedel.widget.widgetservice.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidationErrorResponse {
    List<Violation> violations;
}

