package lt.lb.commons.containers.tables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lt.lb.commons.containers.tables.CellTable.CellFormatIndexCollector;
import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import lt.lb.fastid.FastID;

/**
 *
 * @author laim0nas100
 * @param <Format> decorator object type
 * @param <T> cell content type
 */
public class CellFormatBuilder<Format, T> {

    protected CellFormatBuilder(CellTable<Format, T> table, List<CellPrep<T>> prep) {
        cells = new HashSet<>();
        cells.addAll(prep);
        this.table = table;
    }
    protected CellTable<Format, T> table;
    protected Set<CellPrep<T>> cells;
    protected Formatters<Format> formatters = Formatters.getDefault();

    /**
     * Define formatting action for currently selected cells
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> addFormat(Consumer<Format> cons) {
        for (CellPrep cell : cells) {
            formatters.computeIfAbsent(cell.id, id -> new LinkedList<>()).add(cons);
        }
        return this;
    }

    /**
     * Do with each selected cell
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> forEachCell(Consumer<CellPrep<T>> cons) {
        cells.forEach(cons);
        return this;
    }

    /**
     * Clean all formatters
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanFormat() {
        formatters.clear();
        return this;
    }

    /**
     * Cleans format in only currently selected cells.
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanSelectedFormat() {
        return forEachCell(cell -> {
            formatters.remove(cell.id);
        });
    }

    /**
     * Reset horizontal merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanHorizontalMerge() {
        return this.forEachCell(c -> c.horizontalMerge = TableCellMerge.NONE);
    }

    /**
     * Reset vertical merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanVerticalMerge() {
        return this.forEachCell(c -> c.verticalMerge = TableCellMerge.NONE);
    }

    /**
     * Append selection
     *
     * @return
     */
    public CellFormatIndexCollector<Format, T> addToSelection() {
        return table.selectCells(Optional.of(this));
    }

    /**
     * Deselect currently selected cells
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanSelection() {
        this.cells.clear();
        return this;
    }

    /**
     * Cleans current selection and starts new. All defined formatters are
     * preserved.
     *
     * @return
     */
    public CellFormatIndexCollector<Format, T> cleanSelectionStart() {
        return this.cleanSelection().addToSelection();
    }

    /**
     * Return all collected formatters
     *
     * @return
     */
    public Formatters<Format> getFormatterMap() {
        return this.formatters;
    }

    /**
     * Ability to collect formatters mid-way.
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> withFormatterMap(Consumer<Formatters<Format>> cons) {
        cons.accept(this.getFormatterMap());
        return this;

    }
}
