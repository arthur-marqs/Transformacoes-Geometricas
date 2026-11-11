package cg.raster;

/** Destino de um pixel gerado por um algoritmo de rasterização (coordenadas inteiras do mundo). */
@FunctionalInterface
public interface PixelSink {
    void plot(int x, int y);
}
