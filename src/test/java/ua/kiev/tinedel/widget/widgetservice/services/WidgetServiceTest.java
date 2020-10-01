package ua.kiev.tinedel.widget.widgetservice.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.repositories.WidgetRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ua.kiev.tinedel.widget.widgetservice.utils.DataGenerator.buildWidget;

@SuppressWarnings("DefaultAnnotationParam")
@ExtendWith(MockitoExtension.class)
@FieldDefaults(makeFinal = false, level = AccessLevel.PRIVATE)
class WidgetServiceTest {

    @Mock
    WidgetRepository repository;

    @InjectMocks
    WidgetService service;

    @Test
    void whenSave_ZIndexIsChecked_andUpdated() {
        Widget toSave = buildWidget(UUID.randomUUID()).setZIndex(10);

        when(repository.findByZIndex(10)).thenReturn(Optional.of(buildWidget(UUID.randomUUID())));
        when(repository.save(eq(toSave))).thenAnswer(inv -> inv.getArgument(0, Widget.class));


        Widget res = service.save(toSave);

        assertThat(res).isEqualTo(toSave);
        assertThat(res.getLastModifiedDate()).isNotEqualTo(toSave.getLastModifiedDate());

        verify(repository).acquireWriteLock();
        verify(repository).deleteById(toSave.getId());
        verify(repository).findByZIndex(10);
        verify(repository).updateZIndexToMakeSpaceFor(10);
        verify(repository).save(eq(toSave));
        verify(repository).releaseWriteLock();

        verifyNoMoreInteractions(repository);
    }

    @Test
    void whenSave_ZIndexIsChecked_andNotUpdatedBecauseSameId() {
        Widget toSave = buildWidget(UUID.randomUUID()).setZIndex(10);
        Widget saved = buildWidget(toSave.getId());

        when(repository.findByZIndex(10)).thenReturn(Optional.of(buildWidget(toSave.getId())));
        when(repository.save(toSave)).thenReturn(saved);


        Widget res = service.save(toSave);

        assertThat(res).isSameAs(saved);

        verify(repository).acquireWriteLock();
        verify(repository).deleteById(toSave.getId());
        verify(repository).findByZIndex(10);
        verify(repository).save(toSave);
        verify(repository).releaseWriteLock();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void whenSave_ZIndexIsChecked_andNotUpdatedBecauseNoConflict() {
        Widget toSave = buildWidget(UUID.randomUUID()).setZIndex(10);
        Widget saved = buildWidget(toSave.getId());

        when(repository.findByZIndex(10)).thenReturn(Optional.empty());
        when(repository.save(toSave)).thenReturn(saved);


        Widget res = service.save(toSave);

        assertThat(res).isSameAs(saved);

        verify(repository).acquireWriteLock();
        verify(repository).deleteById(toSave.getId());
        verify(repository).findByZIndex(10);
        verify(repository).save(toSave);
        verify(repository).releaseWriteLock();

        verifyNoMoreInteractions(repository);
    }

    @Test
    void whenGetById_repositoryIsCalled() {
        UUID id = UUID.randomUUID();
        Widget widget = buildWidget(id);
        when(repository.getById(id)).thenReturn(Optional.of(widget));

        assertThat(service.getById(id)).contains(widget);
        verify(repository, only()).getById(id);
    }

    @Test
    void whenFindAll_repositoryIsCalled() {
        List<Widget> widgets = Arrays.stream(new int[] {1,2,3})
                .mapToObj(zIndex -> buildWidget(UUID.randomUUID()).setZIndex(zIndex))
                .collect(Collectors.toList());

        when(repository.findAllOrderByZIndexAsc(24, 12)).thenReturn(widgets);

        assertThat(service.findAll(12, 2)).isEqualTo(widgets);

            verify(repository, only()).findAllOrderByZIndexAsc(24, 12);
    }

    @Test
    void whenDelete_repositoryIsCalled() {
        UUID id = UUID.randomUUID();
        service.deleteById(id);

        verify(repository, only()).deleteById(id);
    }
}