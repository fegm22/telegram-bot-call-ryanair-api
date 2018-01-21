package org.ryanairbot.service;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.ryanairbot.dto.RouteDto;
import org.ryanairbot.integration.RoutesApiClient;
import org.ryanairbot.model.Airport;
import org.ryanairbot.utils.Constants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class RoutesService {

    private RoutesApiClient routesApiClient = new RoutesApiClient(new RestTemplate(),"https://api.ryanair.com/core/3/routes/");

    @Cacheable(Constants.CACHE_ROUTES)
    public DirectedGraph<Airport, DefaultEdge> getAllAvailableRoutes() {
        DirectedGraph<Airport, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        List<RouteDto> routes = routesApiClient.getRoutes();

        routes.stream().forEach(route -> fillGraph(graph, route));

        return graph;
    }

    private void fillGraph(DirectedGraph<Airport, DefaultEdge> graph, RouteDto route) {
        graph.addVertex(route.getAirportFrom());
        graph.addVertex(route.getAirportTo());
        graph.addEdge(route.getAirportFrom(), route.getAirportTo());
    }
}
