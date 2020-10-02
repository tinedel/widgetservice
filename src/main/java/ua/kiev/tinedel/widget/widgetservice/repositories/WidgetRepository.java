package ua.kiev.tinedel.widget.widgetservice.repositories;

import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.rtree.MBRectangle;

import java.util.*;

public interface WidgetRepository {
    Optional<Widget> getById(UUID id);
    List<Widget> findAllOrderByZIndexAsc(int offset, int limit);
    List<Widget> findAllInArea(MBRectangle boundingBox);
    Widget save(Widget widget);
    Optional<Widget> findByZIndex(int zIndex);
    int updateZIndexToMakeSpaceFor(int zIndex);
    void deleteById(UUID id);
    void acquireWriteLock();
    void releaseWriteLock();
    void clear();
}
