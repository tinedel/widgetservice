package ua.kiev.tinedel.widget.widgetservice.repositories;

import org.junit.jupiter.api.Test;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.utils.DataGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.kiev.tinedel.widget.widgetservice.utils.DataGenerator.buildWidget;

class InMemoryWidgetRepositoryTest {

    @Test
    void whenWidgetIsAdded_idIsGeneratedAndItCanBeFoundByIdOrZIndex() {
        WidgetRepository wr = new InMemoryWidgetRepository();

        Widget widget = buildWidget(null);

        widget = wr.save(widget);

        assertThat(wr.getById(widget.getId()))
                .isPresent()
                .contains(widget);

        assertThat(wr.findByZIndex(widget.getZIndex()))
                .isPresent()
                .contains(widget);

        assertThat(widget.getId()).isNotNull();
    }

    @Test
    void whenWidgetIsAddedWithId_idIsUsedAndItCanBeFoundByIdOrZIndex() {
        WidgetRepository wr = new InMemoryWidgetRepository();

        Widget widget = buildWidget(UUID.randomUUID());

        widget = wr.save(widget);

        assertThat(wr.getById(widget.getId()))
                .isPresent()
                .contains(widget);

        assertThat(wr.findByZIndex(widget.getZIndex()))
                .isPresent()
                .contains(widget);

        assertThat(widget.getId()).isNotNull();
    }

    @Test
    void whenWidgetIsSavedWithSameIdAndZIndex_Success() {
        UUID id = UUID.randomUUID();
        Widget existing = buildWidget(id);
        Widget updated = buildWidget(id).setX(10).setY(10);

        WidgetRepository wr = new InMemoryWidgetRepository();

        wr.save(existing);

        wr.save(updated);

        assertThat(wr.getById(id)).contains(updated);
    }

    @Test
    void whenWidgetIsSavedWithDifferentIdSameZIndex_exceptionIsThrown() {
        Widget existing = buildWidget(UUID.randomUUID());
        Widget updated = buildWidget(UUID.randomUUID()).setX(10).setY(10);

        WidgetRepository wr = new InMemoryWidgetRepository();

        wr.save(existing);

        assertThatThrownBy(() -> wr.save(updated)).isInstanceOf(DataStoreException.class);
    }

    @Test
    void whenUpdateZIndexToMakeSpaceForMissingZIndexEmpty_nothingChanges() {
        Widget existing1 = buildWidget(UUID.randomUUID()).setZIndex(-1);
        Widget existing2 = buildWidget(UUID.randomUUID()).setZIndex(1);

        WidgetRepository wr = new InMemoryWidgetRepository();
        wr.save(existing1);
        wr.save(existing2);

        assertThat(wr.updateZIndexToMakeSpaceFor(0)).isEqualTo(0);

        assertThat(wr.findAllOrderByZIndexAsc().stream().map(Widget::getZIndex)).containsSequence(-1, 1);
    }

    @Test
    void whenUpdateZIndexToMakeSpaceForMissingZIndex_everythingMovesUpToNearestBlank() {

        List<Widget> widgets = Arrays.stream(new int[]{-1, 0, 1, 2, 4})
                .mapToObj(it -> buildWidget(UUID.randomUUID()).setZIndex(it))
                .collect(Collectors.toList());

        WidgetRepository wr = new InMemoryWidgetRepository();
        widgets.forEach(wr::save);

        assertThat(wr.updateZIndexToMakeSpaceFor(0)).isEqualTo(3);

        assertThat(wr.findAllOrderByZIndexAsc().stream().map(Widget::getZIndex)).containsSequence(-1, 1, 2, 3, 4);
    }

    @Test
    void whenWidgetIsRemoved_itCanNotBeFound() {
        Widget w = buildWidget(UUID.randomUUID());

        WidgetRepository wr = new InMemoryWidgetRepository();
        wr.save(w);
        wr.deleteById(w.getId());

        assertThat(wr.getById(w.getId())).isEmpty();
        assertThat(wr.findByZIndex(w.getZIndex())).isEmpty();
    }
}