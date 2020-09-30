package ua.kiev.tinedel.widget.widgetservice.models;

import lombok.*;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class ValidationErrorResponse {
    List<Violation> violations;
}

