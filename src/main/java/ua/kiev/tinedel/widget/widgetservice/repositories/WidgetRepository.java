package ua.kiev.tinedel.widget.widgetservice.repositories;

import ua.kiev.tinedel.widget.widgetservice.models.Widget;

import java.util.*;

public interface WidgetRepository {
    Optional<Widget> getById(UUID id);
    List<Widget> findAllOrderByZIndexAsc();
    Widget save(Widget widget);
    Optional<Widget> findByZIndex(int zIndex);
    int updateZIndexToMakeSpaceFor(int zIndex);
    void deleteById(UUID id);
    void acquireWriteLock();
    void releaseWriteLock();
    void clear();
}
