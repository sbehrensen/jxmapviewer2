package org.jxmapviewer.viewer;

import org.jxmapviewer.viewer.util.GeoUtil;

public class CacheTileFactory extends DefaultTileFactory {
    /**
     * Creates a new instance of DefaultTileFactory using the spcified TileFactoryInfo
     *
     * @param info a TileFactoryInfo to configure this TileFactory
     */
    public CacheTileFactory(TileFactoryInfo info) {
        super(info);
    }


    @Override
    Tile getTile(int tpx, int tpy, int zoom, boolean eagerLoad) {
        // wrap the tiles horizontally --> mod the X with the max width
        // and use that
        int tileX = tpx;// tilePoint.getX();
        int numTilesWide = (int) getMapSize(zoom).getWidth();
        if (tileX < 0) {
            tileX = numTilesWide - (Math.abs(tileX) % numTilesWide);
        }

        tileX = tileX % numTilesWide;
        int tileY = tpy;
        // TilePoint tilePoint = new TilePoint(tileX, tpy);
        String url = getInfo().getTileUrl(tileX, tileY, zoom);// tilePoint);
        // System.out.println("loading: " + url);
        String tileID = getTileID(tileX, tileY, zoom);

        Tile.Priority pri = Tile.Priority.High;
        if (!eagerLoad)
        {
            pri = Tile.Priority.Low;
        }
        Tile tile;
        // System.out.println("testing for validity: " + tilePoint + " zoom = " + zoom);
        if (!tileMap.containsKey(tileID)) {

            if (!GeoUtil.isValidTile(tileX, tileY, zoom, getInfo())) {
                tile = new CacheTile(tileX, tileY, zoom);
            }
            else {
                tile = new CacheTile(tileX, tileY, zoom, url, pri, this);
                startLoading(tile);
            }
            tileMap.put(tileID, tile);
        } else {
            tile = tileMap.get(tileID);
            // if its in the map but is low and isn't loaded yet
            // but we are in high mode
            if (tile.getPriority() == Tile.Priority.Low && eagerLoad && !tile.isLoaded()) {
                // System.out.println("in high mode and want a low");
                // tile.promote();
                promote(tile);
            }

            ((CacheTile) tile).setURL(url);
        }

        /*
         * if (eagerLoad && doEagerLoading) { for (int i = 0; i<1; i++) { for (int j = 0; j<1; j++) { // preload the 4
         * tiles under the current one if(zoom > 0) { eagerlyLoad(tilePoint.getX()*2, tilePoint.getY()*2, zoom-1);
         * eagerlyLoad(tilePoint.getX()*2+1, tilePoint.getY()*2, zoom-1); eagerlyLoad(tilePoint.getX()*2,
         * tilePoint.getY()*2+1, zoom-1); eagerlyLoad(tilePoint.getX()*2+1, tilePoint.getY()*2+1, zoom-1); } } } }
         */

        return tile;
    }


    private String getTileID(Tile tile){
        return getTileID(tile.getX(), tile.getY(), tile.getZoom());
    }

    private String getTileID(int x, int y, int zoom){
        return " "+x+" "+y+" "+zoom+" ";
    }
}
