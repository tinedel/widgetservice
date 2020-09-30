package ua.kiev.tinedel.widget.widgetservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;
import ua.kiev.tinedel.widget.widgetservice.repositories.WidgetRepository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.tinedel.widget.widgetservice.utils.DataGenerator.buildWidget;

@SpringBootTest
@AutoConfigureMockMvc
public class WidgetControllerIT {

    @Autowired
    @NonFinal
    MockMvc mockMvc;

    @Autowired
    @NonFinal
    WidgetRepository repository;

    @Autowired
    @NonFinal
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.clear();
    }

    @Test
    @SneakyThrows
    void initialWidgetListIsEmpty() {
        mockMvc.perform(get("/widgets"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @SneakyThrows
    void widgetsOrderedByZIndexOnGet() {
        List<Widget> widgets = Arrays.stream(new int[]{-1, 0, 1, 2, 4})
                .mapToObj(it -> buildWidget(null).setZIndex(it))
                .collect(Collectors.toList());

        widgets.stream()
                .sorted(Comparator.comparing(Widget::getZIndex).reversed())
                .forEach(repository::save);


        mockMvc.perform(get("/widgets"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(widgets), true));
    }

    @Test
    @SneakyThrows
    void widgetsInsertShiftsOthersWidgets() {
        List<Widget> widgets = Arrays.stream(new int[]{-1, 0, 1, 2, 4})
                .mapToObj(it -> buildWidget(null).setZIndex(it))
                .collect(Collectors.toList());

        widgets.forEach(repository::save);


        String resp = mockMvc.perform(
                post("/widgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildWidget(null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").exists())
                .andExpect(jsonPath(".zindex").value(Matchers.contains(0)))
                .andReturn().getResponse().getContentAsString();

        Widget widget = objectMapper.readValue(resp, Widget.class);

        widgets.add(1, widget);
        widgets.get(2).setZIndex(1);
        widgets.get(3).setZIndex(2);
        widgets.get(4).setZIndex(3);

        mockMvc.perform(get("/widgets"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(widgets), true));
    }

    @Test
    @SneakyThrows
    void widgetsDeleteRemovesWidget() {
        List<Widget> widgets = Arrays.stream(new int[]{-1, 0, 1, 2, 4})
                .mapToObj(it -> buildWidget(null).setZIndex(it))
                .collect(Collectors.toList());

        widgets.forEach(repository::save);


        mockMvc.perform(delete("/widgets/{id}", widgets.get(2).getId()))
                .andExpect(status().isOk());

        widgets.remove(2);

        mockMvc.perform(get("/widgets"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(widgets), true));
    }

    @Test
    @SneakyThrows
    void putEditsWidget() {
        List<Widget> widgets = Arrays.stream(new int[]{-1, 0, 1, 2, 4})
                .mapToObj(it -> buildWidget(null).setZIndex(it))
                .collect(Collectors.toList());

        widgets.forEach(repository::save);


        String resp = mockMvc.perform(
                put("/widgets/{id}", widgets.get(3).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(widgets.get(3).withZIndex(0))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        widgets.remove(3);
        Widget edited = objectMapper.readValue(resp, Widget.class);
        widgets.add(1, edited);
        widgets.get(2).setZIndex(1);
        widgets.get(3).setZIndex(2);

        mockMvc.perform(get("/widgets"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(widgets), true));
    }

    @Test
    @SneakyThrows
    void getWithMissingWidgetFails() {
        Widget widget = buildWidget(UUID.randomUUID());
        repository.save(widget);

        mockMvc.perform(get("/widgets/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getWithPresentWidgetSucceeds() {
        Widget widget = buildWidget(UUID.randomUUID());
        repository.save(widget);

        mockMvc.perform(get("/widgets/{id}", widget.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(widget)));
    }

    static final String POST_FIELD_PREFIX = "create.widget.";
    static final String PUT_FIELD_PREFIX = "update.widget.";

    private String[] getFieldsFullNames(String[] violatedFields, String postFieldPrefix) {
        return Arrays.stream(violatedFields).map(field -> postFieldPrefix + field).toArray(String[]::new);
    }

    @Test
    @SneakyThrows
    void requiredFieldsAreValidatedOnPostNoIdValidated() {
        String[] violatedFields = {"x", "y", "zIndex", "width", "height"};
        mockMvc.perform(post("/widgets").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.violations[*].fieldName").value(Matchers.containsInAnyOrder(
                        getFieldsFullNames(violatedFields, POST_FIELD_PREFIX)
                )));
    }

    @Test
    @SneakyThrows
    void idShouldNotBeGivenOnPost() {
        String[] violatedFields = {"id"};
        mockMvc.perform(post("/widgets").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildWidget(UUID.randomUUID()))))
                .andExpect(jsonPath("$.violations[*].fieldName").value(Matchers.containsInAnyOrder(
                        getFieldsFullNames(violatedFields, POST_FIELD_PREFIX)
                )));
    }

    @Test
    @SneakyThrows
    void widthAndHeightMustBePositive() {
        String[] violatedFields = {"width", "height"};
        mockMvc.perform(post("/widgets").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildWidget(null)
                                .setWidth(-1)
                                .setHeight(-2))))
                .andExpect(jsonPath("$.violations[*].fieldName").value(Matchers.containsInAnyOrder(
                        getFieldsFullNames(violatedFields, POST_FIELD_PREFIX)
                )));
    }

    @Test
    @SneakyThrows
    void requiredFieldsAreValidatedOnPutNoIdValidated() {
        String[] violatedFields = {"x", "y", "zIndex", "width", "height", "id"};
        mockMvc.perform(put("/widgets/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.violations[*].fieldName").value(Matchers.containsInAnyOrder(
                        getFieldsFullNames(violatedFields, PUT_FIELD_PREFIX)
                )));
    }


}
