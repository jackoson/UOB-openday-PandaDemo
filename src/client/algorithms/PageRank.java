package client.algorithms;

import scotlandyard.*;

import java.util.*;
import java.io.IOException;

/**
 * A class to calculate the PageRank for each of the nodes in the graph.
 */

public class PageRank {
    
    private Graph<Integer, Route> graph;
    private Map<Node<Integer>, Double> pageRanks;
    private Set<Node<Integer>> nodes;
    private Double d;
    private Map<Integer, Set<Edge<Integer, Route>>> edgeMap;
    
    /**
     * Constructs a new PageRank object.
     *
     * @param graph the graph containing the nodes.
     */
    public PageRank(Graph<Integer, Route> graph) {
        this.graph = graph;
        pageRanks = new HashMap<Node<Integer>, Double>();
        d = new Double(0.85);
        edgeMap = mapEdges();
        initialiseMap();
    }
    
    // Initialises the Map by setting the PageRanks of all nodes to 0.
    private void initialiseMap() {
        nodes = graph.getNodes();
        for (Node<Integer> node : nodes) {
            pageRanks.put(node, new Double(0.0));
        }
    }
    
    /**
     * Completes n iterations of the algorithm to calculate the new
     * PageRank for all nodes.
     *
     * @param iterNo the number of iterations to perform.
     */
    public void iterate(int iterNo) {
        for (int i = 0; i < iterNo; i++) {
            iterate();
        }
    }
    
    /**
     * Completes one iteration of the algorithm to calculate the new
     * PageRank for all nodes.
     */
    public void iterate() {
        Map<Node<Integer>, Double> updatedPageRanks = new HashMap<Node<Integer>, Double>();
        for (Map.Entry<Node<Integer>, Double> entry : pageRanks.entrySet()) {
            Node<Integer> node = entry.getKey();
            Set<Edge<Integer, Route>> edges = edgeMap.get(node.data());
            Double newPageRank = (1 - d) + (d * sumPageRanks(node.data(), edges));
            updatedPageRanks.put(node, newPageRank);
        }
        pageRanks = updatedPageRanks;
    }
    
    // Returns the sum of the PageRank's of the edges of a node.
    // @param currentLocation the current node's location.
    // @param edges the Set of edges a node has.
    // @return the sum of the PageRank's of the edges of a node.
    private Double sumPageRanks(Integer currentLocation, Set<Edge<Integer, Route>> edges) {
        Double sum = new Double(0.0);
        for (Edge<Integer, Route> edge : edges) {
            Integer location = edge.source();
            if (location.equals(currentLocation)) {
                location = edge.target();
            }
            Node<Integer> node = graph.getNode(location);
            Set<Edge<Integer, Route>> nodeEdges = edgeMap.get(node.data());
            sum += getPageRank(node.data()) / (double) nodeEdges.size();
        }
        return sum;
    }
    
    // Returns a Map which maps the Nodes to their corresponding Set of Edges.
    // @return a Map which maps the Nodes to their corresponding Set of Edges.
    private Map<Integer, Set<Edge<Integer, Route>>> mapEdges() {
        Map<Integer, Set<Edge<Integer, Route>>> map = new HashMap<Integer, Set<Edge<Integer, Route>>>();
        Set<Edge<Integer, Route>> edges = graph.getEdges();
        for (Edge<Integer, Route> edge : edges) {
            Integer target = edge.target();
            Integer source = edge.source();
            
            Set<Edge<Integer, Route>> targetSet = map.get(target);
            if (targetSet == null) targetSet = new HashSet<Edge<Integer, Route>>();
            targetSet.add(edge);
            map.put(target, targetSet);
            
            Set<Edge<Integer, Route>> sourceSet = map.get(source);
            if (sourceSet == null) sourceSet = new HashSet<Edge<Integer, Route>>();
            sourceSet.add(edge);
            map.put(source, sourceSet);
        }
        return map;
    }
    
    /**
     * Returns the PageRank for a given node.
     *
     * @param location the location of the node.
     * @return the PageRank for a given node.
     */
    public Double getPageRank(Integer location) {
        Node<Integer> node = graph.getNode(location);
        return pageRanks.get(node);
    }
    
}