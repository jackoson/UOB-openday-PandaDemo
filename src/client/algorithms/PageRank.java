package client.algorithms;

import client.scotlandyard.*;

import java.util.*;
import java.io.IOException;

/**
 * A class to calculate the PageRank for each of the nodes in the graph.
 */

public class PageRank {
    
    private Graph<Integer, Route> graph;
    private Map<Node<Integer>, Double> pageRanks;
    private List<Node<Integer>> nodes;
    private Double d;
    
    /**
     * Constructs a new PageRank object.
     *
     * @param graph the graph containing the nodes.
     */
    public PageRank(Graph<Integer, Route> graph) {
        this.graph = graph;
        pageRanks = new HashMap<Node<Integer>, Double>();
        d = new Double(0.85);
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
            List<Edge<Integer, Route>> edges = graph.getEdges(node.data());
            Double newPageRank = (1 - d) + (d * sumPageRanks(node.data(), edges));
            updatedPageRanks.put(node, newPageRank);
        }
        pageRanks = updatedPageRanks;
    }
    
    // Returns the sum of the PageRank's of the edges of a node.
    // @param currentLocation the current node's location.
    // @param edges the List of edges a node has.
    // @return the sum of the PageRank's of the edges of a node.
    private Double sumPageRanks(Integer currentLocation, List<Edge<Integer, Route>> edges) {
        Double sum = new Double(0.0);
        for (Edge<Integer, Route> edge : edges) {
            Integer location = edge.source();
            if (location.equals(currentLocation)) {
                location = edge.target();
            }
            Node<Integer> node = getNode(location);
            List<Edge<Integer, Route>> nodeEdges = graph.getEdges(node.data());
            sum += getPageRank(node.data()) / (double) nodeEdges.size();
        }
        return sum;
    }
    
    /**
     * Returns the PageRank for a given node.
     *
     * @param location the location of the node.
     * @return the PageRank for a given node.
     */
    public Double getPageRank(Integer location) {
        Node<Integer> node = getNode(location);
        return pageRanks.get(node);
    }
    
    // Returns the node in the graph with the given location.
    // Returns null if there is no node in the graph.
    // Needed as supplied code is incorrect.
    // @param id the location of the node to be found.
    // @return the node in the graph with the given location.
    private Node<Integer> getNode(Integer id) {
        for (Node<Integer> node : nodes) {
            if (node.data().equals(id)) {
              return node;
            }
        }
        return null;
    }
    
}