package ua.kiev.tinedel.widget.widgetservice.repositories;

import org.springframework.stereotype.Service;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class InMemoryWidgetRepository implements WidgetRepository {

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Map<UUID, Widget> idIndex = new HashMap<>(); // no requirement to maintain order on id
    NavigableMap<Integer, Widget> zIndexIndex = new TreeMap<>();

    @Override
    public Optional<Widget> getById(UUID id) {
        try {
            lock.readLock().lock();
            return Optional.ofNullable(idIndex.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Widget> findAllOrderByZIndexAsc() {
        try {
            lock.readLock().lock();
            return List.copyOf(zIndexIndex.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Widget save(Widget widget) {
        try {
            acquireWriteLock();
            if (widget.getId() == null) {
                widget.setId(UUID.randomUUID());
            }
            // check zIndex:
            if (findByZIndex(widget.getZIndex())
                    .filter(inDb -> !inDb.getId().equals(widget.getId()))
                    .isPresent()) {
                // found widget with different id with same z index
                throw new DataStoreException("Found another element with same zIndex");
            } else {
                zIndexIndex.put(widget.getZIndex(), widget);
                idIndex.put(widget.getId(), widget);
                return widget;
            }
        } finally {
            releaseWriteLock();
        }
    }

    @Override
    public Optional<Widget> findByZIndex(int zIndex) {
        try {
            lock.readLock().lock();
            return Optional.ofNullable(zIndexIndex.get(zIndex));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int updateZIndexToMakeSpaceFor(int zIndex) {
        try {
            int updated = 0;
            acquireWriteLock();
            Widget current = zIndexIndex.remove(zIndex);
            while (current != null) {
                current.setZIndex(current.getZIndex() + 1);
                current = zIndexIndex.put(current.getZIndex(), current);
                updated++;
            }
            return updated;
        } finally {
            releaseWriteLock();
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            acquireWriteLock();
            Widget removed = idIndex.remove(id);
            if (removed != null) {
                zIndexIndex.remove(removed.getZIndex());
            }
        } finally {
            releaseWriteLock();
        }
    }

    public void acquireWriteLock() {
        lock.writeLock().lock();
    }

    public void releaseWriteLock() {
        lock.writeLock().unlock();
    }

    @Override
    public void clear() {
        try {
            acquireWriteLock();
            idIndex.clear();
            zIndexIndex.clear();
        } finally {
            releaseWriteLock();
        }
    }
}
