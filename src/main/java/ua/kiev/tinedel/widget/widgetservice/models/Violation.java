package ua.kiev.tinedel.widget.widgetservice.models;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class Violation {
    String fieldName;
    String message;
}
