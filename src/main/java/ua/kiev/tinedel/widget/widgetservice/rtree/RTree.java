package ua.kiev.tinedel.widget.widgetservice.rtree;

import lombok.experimental.NonFinal;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RTree {

    public final static int MINIMUM_FILL = 5;
    public final static int MAXIMUM_FILL = 100;

    @NonFinal Node root = new NonLeafNode();

    public List<Widget> find(MBRectangle query) {
        return root.find(query);
    }

    public void add(Widget w) {
        root.add(w);
    }

    public void delete(Widget w) {
        root.delete(w);
    }

    public void validate(int expectedSize) {
        if (expectedSize != size()) {
            throw new RuntimeException("Some elements were lost");
        }

        root.validate();
    }

    public int size() {
        return root.size();
    }

    public void clear() {
        root = new NonLeafNode();
    }

    private abstract static class Node {
        @NonFinal
        protected MBRectangle mbr;

        public Node(MBRectangle mbr) {
            this.mbr = mbr;
        }

        public Node() {
            this(null);
        }

        public abstract List<Widget> find(MBRectangle query);

        public abstract void add(Widget w);

        public abstract void delete(Widget w);

        public abstract Node fixOverflow();

        public abstract int size();

        public abstract void validate();
    }

    private static class NonLeafNode extends Node {

        Node[] children = new Node[9];

        public NonLeafNode() {
            for (int i = 0; i < 9; i++) {
                children[i] = new LeafNode();
            }
        }

        public NonLeafNode(Node[] seed) {
            if (seed.length != 9) {
                throw new RuntimeException("Overfill fix error. Must produce 9 child nodes");
            }

            System.arraycopy(seed, 0, children, 0, seed.length);
            updateMbr();
        }

        @Override
        public List<Widget> find(MBRectangle query) {
            return Arrays.stream(children)
                    .filter(child -> child.mbr != null)
                    .filter(child -> query.intersects(child.mbr) || query.contains(child.mbr) || child.mbr.contains(query))
                    .flatMap(child -> child.find(query).stream())
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public void add(Widget w) {
            MBRectangle toAdd = MBRectangle.from(w);

            int chosen = choseChild(toAdd);
            children[chosen].add(w);
            children[chosen] = children[chosen].fixOverflow();
            mbr = Optional.ofNullable(mbr).map(m -> m.append(toAdd)).orElse(toAdd);
        }

        public int choseChild(MBRectangle toAdd) {

            int minDeltaAreaIndex = -1;
            long minDeltaArea = Long.MAX_VALUE;
            long minArea = Integer.MAX_VALUE;

            long addingArea = toAdd.area();

            for (int i = 0; i < children.length; i++) {
                Node child = children[i];

                // if contains put it there
                if (Optional.ofNullable(child.mbr).map(c -> c.contains(toAdd)).orElse(false)) {
                    return i;
                }

                long newArea = Optional.ofNullable(child.mbr).map(c -> c.append(toAdd).area()).orElse(addingArea);
                long oldArea = Optional.ofNullable(child.mbr).map(MBRectangle::area).orElse(0L);

                if (newArea - oldArea < minDeltaArea) {
                    minDeltaArea = newArea - oldArea;
                    minArea = oldArea;
                    minDeltaAreaIndex = i;
                } else if (newArea - oldArea == minDeltaArea && oldArea < minArea) {
                    minArea = oldArea;
                    minDeltaAreaIndex = i;
                }
            }
            return minDeltaAreaIndex;
        }

        @Override
        public void delete(Widget w) {
            MBRectangle query = MBRectangle.from(w);
            Arrays.stream(children)
                    .filter(child -> child.mbr != null)
                    .filter(child -> child.mbr.intersects(query) || child.mbr.contains(query) || query.contains(child.mbr))
                    .forEach(n -> n.delete(w));

            updateMbr();

        }

        public void updateMbr() {
            mbr = Arrays.stream(children).filter(n -> n.mbr != null)
                    .map(n -> n.mbr)
                    .reduce(null,
                            (a, b) -> Optional.ofNullable(a).map(nna -> nna.append(b)).orElse(b),
                            (a, b) -> Optional.ofNullable(a).map(nna -> nna.append(b)).orElse(b));
        }

        @Override
        public Node fixOverflow() {
            return this;
        }

        @Override
        public int size() {
            return Arrays.stream(children).mapToInt(Node::size).sum();
        }

        @Override
        public void validate() {
            final List<Node> nonCompliantChildren = Arrays.stream(children).filter(c -> !mbr.contains(c.mbr))
                    .collect(Collectors.toUnmodifiableList());

            if (!nonCompliantChildren.isEmpty()) {
                throw new RuntimeException("One of the node's child is not in node's mbr");
            }
        }
    }

    private static class LeafNode extends Node {

        List<Widget> content = new ArrayList<>();

        public LeafNode(MBRectangle mbr) {
            super(mbr);
        }

        public LeafNode() {
            super();
        }

        @Override
        public List<Widget> find(MBRectangle query) {
            return content.stream().filter(w -> query.contains(MBRectangle.from(w)))
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public void add(Widget w) {
            content.add(w);
            MBRectangle toAppend = MBRectangle.from(w);
            mbr = Optional.ofNullable(mbr).map(e -> e.append(toAppend)).orElse(toAppend);
        }

        @Override
        public void delete(Widget w) {
            content.remove(w);
            if (content.isEmpty()) {
                mbr = null;
            } else {
                int minx = Integer.MAX_VALUE;
                int miny = Integer.MAX_VALUE;
                int maxx = Integer.MIN_VALUE;
                int maxy = Integer.MIN_VALUE;

                for (Widget widget : content) {
                    if (widget.getX() < minx) minx = widget.getX();
                    if (widget.getY() < miny) miny = widget.getY();
                    if (widget.getX() + widget.getWidth() > maxx) maxx = widget.getX() + widget.getWidth();
                    if (widget.getY() + widget.getHeight() > maxy) maxy = widget.getY() + widget.getHeight();
                }

                mbr = new MBRectangle(minx, miny, maxx, maxy);
            }
        }

        @Override
        public Node fixOverflow() {
            if (content.size() < MAXIMUM_FILL) {
                return this;
            }
            return split();
        }

        @Override
        public int size() {
            return content.size();
        }

        @Override
        public void validate() {
            final List<MBRectangle> notCompliantContent = content.stream().map(MBRectangle::from)
                    .filter(cmbr -> !mbr.contains(cmbr)).collect(Collectors.toUnmodifiableList());

            if (!notCompliantContent.isEmpty()) {
                throw new RuntimeException("Leaf node contains widget out of mbr");
            }
        }

        public Node split() {
            NonLeafNode nlf = new NonLeafNode(findMostDistinctWidgets(content));
            content.forEach(nlf::add);
            return nlf;
        }

        public Node[] findMostDistinctWidgets(List<Widget> content) {
            final List<Widget> sortedByX = content.stream().sorted(Comparator.comparing(Widget::getX))
                    .collect(Collectors.toUnmodifiableList());
            return IntStream.range(0, 9)
                    .map(i -> i * MAXIMUM_FILL / 9)
                    .mapToObj(sortedByX::get)
                    .map(w -> new LeafNode(MBRectangle.from(w)))
                    .toArray(Node[]::new);
        }
    }


}
