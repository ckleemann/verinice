/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator;

import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_DESCRIPTION;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_A;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_C;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_I;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_TITLE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.TraversalFilter;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.IPropertyAdapter;
import sernet.verinice.service.linktable.PropertyAdapterFactory;
import sernet.verinice.service.linktable.generator.mergepath.Path;
import sernet.verinice.service.linktable.generator.mergepath.VqlAst;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Flatten a {@VeriniceGraph} path to a list.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class LtrPrintRowsTraversalListener implements sernet.verinice.interfaces.graph.TraversalListener {

    private static final Logger LOG = Logger.getLogger(LtrPrintRowsTraversalListener.class);

    private Queue<CnATreeElement> cnaTreeElementQueue = new LinkedList<>();
    private Map<CnATreeElement, Edge> incomingEdges = new HashMap<>();
    private Path path;
    private final VeriniceGraph dataGraph;
    private VeriniceGraphResult result = new VeriniceGraphResult();

    private Set<String> columnHeader;

    // use this filter to detect leafs in the verince data graph.
    private TraversalFilter filter;

    /**
     * Flatten a {@VeriniceGraph} path to a list. The result is available with
     *
     *
     * @param path
     *            A path generated by {@link VqlAst}.
     * @param filter
     *            Use this filter for detecting leafs in the tree
     * 
     * @param graph
     *            The verinice data graph.
     * 
     * @param columnHeader
     *            A list of all header of a LTR-Table
     */
    LtrPrintRowsTraversalListener(Path path, TraversalFilter filter, VeriniceGraph graph,
            Set<String> columnHeader, VeriniceGraphResult result) {
        this.path = path;
        this.filter = filter;
        this.dataGraph = graph;
        this.columnHeader = columnHeader;
        this.result = result;
    }

    @Override
    public void nodeTraversed(CnATreeElement node, int depth) {
        LOG.debug("traversed node: " + node.getTitle() + ":" + node.getTypeId());
        cnaTreeElementQueue.add(node);
    }

    @Override
    public void nodeFinished(CnATreeElement node, int depth) {

        LOG.debug("finished node: " + node.getTitle() + ":" + node.getTypeId());

        if (isPathLeaf(depth)) {
            buildRow();

            result.getCompletelyTraversedRows().add(buildRow());
        } else {
            if (isLeafForElement(node, depth)) {
                result.getPartlyTraversedRows().add(buildRow());
            }
        }

        cnaTreeElementQueue.remove(node);
        incomingEdges.remove(node);
    }

    private boolean isPathLeaf(int depth) {
        return depth == path.getPathElements().size() - 1;
    }

    /**
     * Returns wether the element-node has any outgoing edges left.
     */
    private boolean isLeafForElement(CnATreeElement node, int depth) {
        Set<Edge> allEdgesOfNode = dataGraph.getGraph().edgesOf(node);
        boolean isLeaf = true;
        for (Edge edge : allEdgesOfNode) {
            CnATreeElement target = edge.getSource() == node? edge.getTarget() : edge.getSource();
            CnATreeElement source = edge.getSource() != node? edge.getTarget() : edge.getSource();
            isLeaf &= !filter.edgeFilter(edge, source, target, depth);
        }

        return isLeaf;
    }

    private Map<String, String> buildRow() {

        LOG.debug("is printed: " + path);

        Map<String, String> row = initRow();
        Iterator<CnATreeElement> iterator = cnaTreeElementQueue.iterator();
        int i = 0;

        while (iterator.hasNext()) {
            CnATreeElement next = iterator.next();
            VqlNode pathElement = path.getPathElements().get(i).node;
            VqlEdge incomingEdge = path.getPathElements().get(i).edge;
            printRow(row, next, pathElement);
            printRow(row, next, incomingEdge);

            i++;
        }
        return row;

    }

    private void printRow(Map<String, String> row, CnATreeElement node, VqlEdge incominVqlgEdge) {

        if (incominVqlgEdge != null && incominVqlgEdge.isMatch()) {

            Edge edge = incomingEdges.get(node);

            for (String propertyType : incominVqlgEdge.getPropertyTypes()) {

                String pathforProperty = incominVqlgEdge.getPathforProperty(propertyType);

                if (!row.containsKey(pathforProperty)) {
                    LOG.error("this column path is not defined for this table: " + pathforProperty);
                    return;
                }

                if (TYPE_RISK_VALUE_C.equals(propertyType)) {
                    row.put(pathforProperty, String.valueOf(edge.getRiskConfidentiality()));
                }

                else if (TYPE_RISK_VALUE_I.equals(propertyType)) {
                    row.put(pathforProperty, String.valueOf(edge.getRiskIntegrity()));
                }

                else if (TYPE_RISK_VALUE_A.equals(propertyType)) {
                    row.put(pathforProperty, String.valueOf(edge.getRiskAvailability()));
                }

                else if (TYPE_DESCRIPTION.equals(propertyType)) {
                    row.put(pathforProperty, edge.getDescription());
                }

                else if (TYPE_TITLE.equals(propertyType)) {
                    handleTitle(row, node, edge, pathforProperty);
                }
            }
        }
    }

    private void handleTitle(Map<String, String> row, CnATreeElement node, Edge edge, String pathforProperty) {
        Direction direction = getDirection(edge, node);
        String edgeTitle = getEdgeTitle(edge, direction);
        row.put(pathforProperty, edgeTitle);
    }

    private void printRow(Map<String, String> row, CnATreeElement element, VqlNode pathElement) {
        if (pathElement.isMatch()) {
            for (String propertyType : pathElement.getPropertyTypes()) {
                IPropertyAdapter adapter = PropertyAdapterFactory.getAdapter(element);
                String propertyValue = adapter.getPropertyValue(propertyType);

                String keyInRow = pathElement.getPathForProperty(propertyType);
                if (row.containsKey(keyInRow)) {
                    row.put(keyInRow, propertyValue);
                    LOG.debug("Add value " + propertyValue + " to result set: " + row);
                } else {
                    LOG.error("Key " + keyInRow + " is not defined for row " + row);
                }

            }
        }
    }

    private Map<String, String> initRow() {
        Map<String, String> row = new TreeMap<>(new NumericStringComparator());
        for (String id : columnHeader) {
            row.put(id, "");
        }
        return row;
    }

    @Override
    public void edgeTraversed(CnATreeElement source, CnATreeElement target, Edge edge, int depth) {

        LOG.debug("traversed edge: " + edge + " depth: " + depth);

        if (target != null) {
            incomingEdges.put(target, edge);
            LOG.debug("push edge with key: " + target + " -> " + edge);
        }
    }

    /**
     * Returns a row, which is one valid path through the {@link VeriniceGraph}.
     * The map containes a key, which is the column path header:
     *
     * <pre>
     * 1. (assetgroup > asset.title -> Computer 1, assetgroup.title -> IT)
     * 2. (assetgroup > asset.title -> Computer 2, assetgroup.title -> IT)
     * </pre>
     *
     */
    public VeriniceGraphResult getResult() {
        return result;
    }

    enum Direction {
        INCOMING, OUTCOMING
    }

    private Direction getDirection(Edge link, CnATreeElement node) {
        if (link.getSource() == node) {
            return Direction.INCOMING;
        }

        return Direction.OUTCOMING;
    }

    private String getEdgeTitle(Edge link, Direction direction) {
        String linkType = link.getType();
        HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(linkType);
        if (relation == null) {
            return linkType;
        }
        return direction.equals(Direction.OUTCOMING) ? relation.getName() : relation.getReversename();
    }

}