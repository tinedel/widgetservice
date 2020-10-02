package ua.kiev.tinedel.widget.widgetservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.repositories.WidgetRepository;
import ua.kiev.tinedel.widget.widgetservice.rtree.MBRectangle;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WidgetService {
    WidgetRepository repository;

    public Widget save(Widget widget) {
        try {
            repository.acquireWriteLock();

            // remove widget first to avoid unnecessary zindex increments
            if(widget.getId() != null) {
                repository.deleteById(widget.getId());
            }

            if (repository.findByZIndex(widget.getZIndex())
                    .filter(sameZIndex -> !sameZIndex.getId().equals(widget.getId()))
                    .isPresent()) {
                repository.updateZIndexToMakeSpaceFor(widget.getZIndex());
            }

            return repository.save(widget.withLastModifiedDate(Instant.now()));
        } finally {
            repository.releaseWriteLock();
        }
    }

    public Optional<Widget> getById(UUID id) {
        return repository.getById(id);
    }

    public List<Widget> findAll(int pageSize, int pageNumber) {
        int offset = pageNumber * pageSize;
        return repository.findAllOrderByZIndexAsc(offset, pageSize);
    }

    public List<Widget> findInBoundingBox(MBRectangle boundingBox) {
        return repository.findAllInArea(boundingBox).stream()
                .sorted(Comparator.comparing(Widget::getZIndex))
                .collect(Collectors.toUnmodifiableList());
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
