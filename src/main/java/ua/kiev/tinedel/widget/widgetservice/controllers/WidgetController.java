package ua.kiev.tinedel.widget.widgetservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ua.kiev.tinedel.widget.widgetservice.models.CreateValidationGroup;
import ua.kiev.tinedel.widget.widgetservice.models.UpdateValidationGroup;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.services.WidgetService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/widgets", produces = "application/json")
@RequiredArgsConstructor
@Validated
public class WidgetController {

    WidgetService service;

    @GetMapping()
    public List<Widget> findAllWidgets() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Widget getWidget(@PathVariable("id") UUID id) {
        return service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Widget not found"));
    }

    @PostMapping(consumes = "application/json")
    @Validated(CreateValidationGroup.class)
    public Widget create(@Valid @RequestBody Widget widget) {
        return service.save(widget);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @Validated(UpdateValidationGroup.class)
    public Widget update(@PathVariable("id") UUID id, @Valid @RequestBody Widget widget) {
        assert widget.getId().equals(id);

        return service.save(widget);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.deleteById(id);
    }
}
