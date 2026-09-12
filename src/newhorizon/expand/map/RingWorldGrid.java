package newhorizon.expand.map;

import mindustry.graphics.g3d.PlanetGrid;

/**
 * Campaign topology for a finite hexagonal strip wrapped around a ring world.
 * Visual corners are kept per tile so boundary cells can be clipped without
 * forcing the spherical, closed topology used by vanilla planets.
 */
public class RingWorldGrid extends PlanetGrid {
    public RingWorldGrid(RingWorldPlanet planet) {
        super(0);

        size = 0;
        tiles = new Ptile[planet.columns * planet.rows];
        corners = new Corner[tiles.length * 6];
        edges = new Edge[0];

        for (int id = 0; id < tiles.length; id++) {
            Ptile tile = tiles[id] = new Ptile(id, 6);
            planet.getSourcePoint(id, tile.v);

            for (int cornerIndex = 0; cornerIndex < 6; cornerIndex++) {
                Corner corner = new Corner(id * 6 + cornerIndex);
                planet.getSourceCorner(id, cornerIndex, corner.v);
                tile.corners[cornerIndex] = corner;
                corners[corner.id] = corner;
            }
        }

        for (int id = 0; id < tiles.length; id++) {
            Ptile tile = tiles[id];
            int column = id % planet.columns;
            int row = id / planet.columns;
            boolean odd = (column & 1) == 1;

            setNeighbor(tile, 0, planet, column - 1, row + (odd ? 0 : -1));
            setNeighbor(tile, 1, planet, column - 1, row + (odd ? 1 : 0));
            setNeighbor(tile, 2, planet, column, row - 1);
            setNeighbor(tile, 3, planet, column, row + 1);
            setNeighbor(tile, 4, planet, column + 1, row + (odd ? 0 : -1));
            setNeighbor(tile, 5, planet, column + 1, row + (odd ? 1 : 0));
        }
    }

    private void setNeighbor(Ptile tile, int index, RingWorldPlanet planet, int column, int row) {
        column = (column % planet.columns + planet.columns) % planet.columns;

        // Boundary cells keep six visual corners. Missing outward links are
        // folded onto the nearest boundary row, avoiding nulls in Sector.near().
        row = Math.max(0, Math.min(planet.rows - 1, row));
        Ptile neighbor = tiles[row * planet.columns + column];
        tile.tiles[index] = neighbor == tile ? tiles[row * planet.columns + (column + 1) % planet.columns] : neighbor;
    }
}
