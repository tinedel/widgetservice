package ua.kiev.tinedel.widget.widgetservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.repositories.WidgetRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WidgetService {
    WidgetRepository repository;

    public Widget save(Widget widget) {
        try {
            repository.acquireWriteLock();

            if (repository.findByZIndex(widget.getZIndex())
                    .filter(sameZIndex -> !sameZIndex.getId().equals(widget.getId()))
                    .isPresent()) {
                repository.updateZIndexToMakeSpaceFor(widget.getZIndex());
            }

            return repository.save(widget);
        } finally {
            repository.releaseWriteLock();
        }
    }

    public Optional<Widget> getById(UUID id) {
        return repository.getById(id);
    }

    public List<Widget> findAll() {
        return repository.findAllOrderByZIndexAsc();
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
