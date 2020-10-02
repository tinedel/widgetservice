package ua.kiev.tinedel.widget.widgetservice.rtree;

import org.junit.jupiter.api.Test;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bad bad test, overrelies on randomness, but not enough time to create proper tests
 */
class RTreeTest {

    final static int EXTENT = 1000;
    final static int MAX_WH = 500;
    public static final Random RANDOM = new Random();

    List<Widget> generateWidgets(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateWidget())
                .collect(Collectors.toList());
    }

    private Widget generateWidget() {
        return Widget.builder()
                .x(RANDOM.nextInt(2 * EXTENT) - EXTENT)
                .y(RANDOM.nextInt(2 * EXTENT) - EXTENT)
                .width(RANDOM.nextInt(MAX_WH) + 1)
                .height(RANDOM.nextInt(MAX_WH) + 1)
                .build();
    }

    @Test
    public void indexWorksRoughly() {
        RTree tree = new RTree();
        List<Widget> widgets = generateWidgets(1000);

        widgets.forEach(tree::add);
        tree.validate(1000);

        int[] a = RANDOM.ints(0, widgets.size()).limit(20).toArray();
        int[] b = RANDOM.ints(0, widgets.size()).limit(20).toArray();

        for(int i : a) {
            for(int j : b) {
                checkBothFound(widgets.get(i), widgets.get(j), tree);
            }
        }
    }

    @Test
    public void indexWorksWithOneElement() {
        RTree tree = new RTree();
        Widget widget = generateWidget();

        tree.add(widget);

        assertThat(tree.find(new MBRectangle(-1 * EXTENT, -1 * EXTENT,
                EXTENT + MAX_WH, EXTENT + MAX_WH))).contains(widget);
    }

    @Test
    public void removalWorks() {
        RTree tree = new RTree();
        Widget widget = generateWidget();

        tree.add(widget);
        tree.delete(widget);

        assertThat(tree.find(new MBRectangle(-1 * EXTENT, -1 * EXTENT,
                EXTENT + MAX_WH, EXTENT + MAX_WH))).isEmpty();
    }

    private void checkBothFound(Widget a, Widget b, RTree tree) {
        MBRectangle query = MBRectangle.from(a).append(MBRectangle.from(b));

        final List<Widget> actual = tree.find(query);
        assertThat(actual).contains(a, b);
    }

}
