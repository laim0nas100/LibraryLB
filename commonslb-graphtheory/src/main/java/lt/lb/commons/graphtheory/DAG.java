package lt.lb.commons.graphtheory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import lt.lb.commons.misc.rng.RandomDistribution;

/**
 *
 * @author laim0nas100
 */
public class DAG extends Orgraph {

    public boolean addLinkIfNoCycles(GLink link) {
        this.addLink(link);
        if (Algorithms.containsCycleBFS(this, link.nodeTo)) {
            this.removeLink(link.nodeFrom, link.nodeTo);
            return false;
        } else {
            return true;
        }
    }

    public static DAG generateRandomDAGNaive(RandomDistribution rnd, long linkCount, long maxWeight) {
        HashSet triedLinks = new HashSet<>();
        DAG graph = new DAG();
        graph.nodes.put(0l, new GNode(0));
        Double w = new Double(rnd.nextLong(1L, maxWeight));
        while (graph.links.size() < linkCount) {
            long nodeFrom = 0;
            long nodeTo = 0;
            while (nodeTo == nodeFrom) {
                nodeFrom = rnd.nextInt(0, graph.nodes.size() + 1);
                nodeTo = rnd.nextInt(0, graph.nodes.size() + 1);
            }

            GLink link = new GLink(nodeTo, nodeFrom, w);
            if (!triedLinks.contains(link.key())) {
                triedLinks.add(link.key());
                if (graph.addLinkIfNoCycles(link)) {
                    triedLinks.add(link.reverse().key());

                    w = new Double(rnd.nextLong(1L, maxWeight));
                }
            }
        }
        return graph;

    }

    public static DAG generateRandomDAGBetter(RandomDistribution rnd, long linkCount, long maxWeight, double batchSize) {
        DAG graph = new DAG();
        graph.nodes.put(0l, new GNode(0));
        while (graph.links.size() < linkCount) {

            ArrayList<Long> possibleCandidates = new ArrayList<>();
            possibleCandidates.addAll(graph.nodes.keySet());

            HashSet<Long> parentSet;

            long nodeFrom = rnd.nextInt(0, graph.nodes.size() - 1);
            parentSet = Algorithms.getParentSet(graph, nodeFrom);
            parentSet.add(nodeFrom);
            Optional<GNode> node = graph.getNode(nodeFrom);
            if (node.isPresent()) {
                parentSet.addAll(node.get().linksTo);
            }

            possibleCandidates.removeAll(parentSet);
            possibleCandidates.add((long) graph.nodes.size());

            long iterLimit = (long) Math.min(batchSize * possibleCandidates.size(), possibleCandidates.size());
            iterLimit = (long) Math.max(Math.min(linkCount - graph.links.size(), iterLimit), 1);

            Collections.shuffle(possibleCandidates, RandomDistribution.asRandom(rnd));
            ArrayDeque<Long> candidates = new ArrayDeque<>(possibleCandidates);
            for (long i = 0; i < iterLimit; i++) {
                double w = rnd.nextDouble(0D, (double) maxWeight);
                graph.addLink(graph.newLink(nodeFrom, candidates.removeFirst(), w));
            }
        }
        return graph;

    }

}
