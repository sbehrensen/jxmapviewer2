package org.jxmapviewer.viewer;

import org.jxmapviewer.util.GraphicsUtilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;
import java.util.Date;

public class CacheTile extends Tile {
    static final long cacheImageLifeTime = 5000;
    private static final long reloadTime = 2500;
    private static final long fullImageTime = 3000;
    private static final double maxAlphaThreshold = 0.8;

    private Date loadedTime;
    private SoftReference<BufferedImage> cacheImage = new SoftReference<BufferedImage>(null);
    private boolean inprocess = false;

    private double currentAlpha;

    public CacheTile(int x, int y, int zoom) {
        super(x, y, zoom);
        init();
    }

    CacheTile(int x, int y, int zoom, String url, Priority priority, TileFactory dtf) {
        super(x, y, zoom, url, priority, dtf);
        init();
    }

    protected void init() {
        addPropertyChangeListener("loaded", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                loadedTime = new Date();
                BufferedImage current = image.get();
                if (current != null) {
                    cacheImage = new SoftReference<BufferedImage>(deepCopy(current));
                    loadedTime = new Date();
                    inprocess = true;
                }
            }
        });
        currentAlpha = 0;
    }

    @Override
    public BufferedImage getImage() {
        if (loadedTime == null) {
            loadedTime = new Date();
        }

        long currentTime = System.currentTimeMillis();
        long timeDifference = Math.abs(currentTime - loadedTime.getTime());

        //return cacheImage
        if (isLoading() || (timeDifference < cacheImageLifeTime) && cacheImage != null) {
            if (timeDifference < fullImageTime) {
                //show image with full alpha fullTimeImage seconds
                return cacheImage.get();
            } else {
                //compute alpha value based on image life time
                double alpha = (double) timeDifference / (double) cacheImageLifeTime;

                if(alpha > maxAlphaThreshold){
                    alpha = maxAlphaThreshold;
                }
                currentAlpha = alpha;

                cacheImage = new SoftReference<BufferedImage>(GraphicsUtilities.makeImageTranslucent(cacheImage.get(), currentAlpha));

                return cacheImage.get();
            }

        }

        //delete cacheImage if its lifetime
        if (cacheImage != null && timeDifference > cacheImageLifeTime) {
            cacheImage = new SoftReference<BufferedImage>(null);
        }

        //reload this tile
        if (isLoading() == false && isLoaded() &&(timeDifference > reloadTime) && inprocess) {
            if (dtf != null) {
                setLoaded(false);
                dtf.startLoading(this);
            }
            inprocess = false;
        }

        return cacheImage.get();
    }

    void setURL(String url) {
        this.url = url;
    }

    Date getLastTimeLoaded(){
        return loadedTime;
    }
    /**
     * https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
     *
     * @param bi
     * @return
     */
    private BufferedImage deepCopy(BufferedImage bi) {
        if (bi == null) return null;
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
