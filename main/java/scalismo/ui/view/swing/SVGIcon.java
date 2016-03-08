/*
 * SVGIcon.java
 *
 * A Swing Icon that draws an SVG image.
 *
 * Cameron McCormack <cam (at) mcc.id.au>
 *
 * Permission is hereby granted to use, copy, modify and distribte this
 * code for any purpose, without fee.
 *
 * Initial version: April 21, 2005
 */

/*
 * Minimally modified for use in the scalismo UI.
 */

package scalismo.ui.view.swing;

import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;


/**
 * A Swing Icon that draws an SVG image.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 */
public class SVGIcon extends UserAgentAdapter implements Icon {

    /**
     * The BufferedImage generated from the SVG document.
     */
    protected BufferedImage bufferedImage;

    /**
     * The width of the rendered image.
     */
    protected int width;

    /**
     * The height of the rendered image.
     */
    protected int height;

    /**
     * The URI that was used to create this icon.
     */
    private final String uri;

    /**
     * Create a new SVGIcon object.
     *
     * @param uri The URI to read the SVG document from.
     */
    public SVGIcon(String uri) throws TranscoderException {
        this(uri, 0, 0);
    }

    /**
     * Create a new SVGIcon object.
     *
     * @param uri The URI to read the SVG document from.
     * @param w   The width of the icon.
     * @param h   The height of the icon.
     */
    public SVGIcon(String uri, int w, int h) throws TranscoderException {
        this.uri = uri;
        generateBufferedImage(new TranscoderInput(uri), w, h);
    }

    /**
     * Generate the BufferedImage.
     */
    protected void generateBufferedImage(TranscoderInput in, int w, int h)
            throws TranscoderException {
        BufferedImageTranscoder t = new BufferedImageTranscoder();
        if (w != 0 && h != 0) {
            t.setDimensions(w, h);
        }
        t.transcode(in, null);
        bufferedImage = t.getBufferedImage();
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
    }

    /**
     * A transcoder that generates a BufferedImage.
     */
    protected class BufferedImageTranscoder extends ImageTranscoder {

        /**
         * The BufferedImage generated from the SVG document.
         */
        protected BufferedImage bufferedImage;

        /**
         * Creates a new ARGB image with the specified dimension.
         *
         * @param width  the image width in pixels
         * @param height the image height in pixels
         */
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Writes the specified image to the specified output.
         *
         * @param img    the image to write
         * @param output the output where to store the image
         * @throws TranscoderException if an error occured while storing the image
         */
        public void writeImage(BufferedImage img, TranscoderOutput output)
                throws TranscoderException {
            bufferedImage = img;
        }

        /**
         * Returns the BufferedImage generated from the SVG document.
         */
        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        /**
         * Set the dimensions to be used for the image.
         */
        public void setDimensions(int w, int h) {
            hints.put(KEY_WIDTH, new Float(w));
            hints.put(KEY_HEIGHT, new Float(h));
        }
    }

    // Icon //////////////////////////////////////////////////////////////////

    /**
     * Returns the icon's width.
     */
    public int getIconWidth() {
        return width;
    }

    /**
     * Returns the icon's height.
     */
    public int getIconHeight() {
        return height;
    }

    /**
     * Draw the icon at the specified location.
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(bufferedImage, x, y, null);
    }

    // UserAgent /////////////////////////////////////////////////////////////

    /**
     * Returns the default size of this user agent.
     */
    public Dimension2D getViewportSize() {
        return new Dimension(width, height);
    }

    // Scalismo UI adaptation ////////////////////////////////////////////////

    /**
     * Returns an SVGIcon sharing the same source as this one, but with a different size.
     *
     * @param newWidth  requested icon width
     * @param newHeight requested icon height
     * @return a resized version of this icon
     * @throws TranscoderException if anything goes wrong
     */
    public SVGIcon createResized(int newWidth, int newHeight) throws TranscoderException {
        if (newWidth == width && newHeight == height) {
            return this;
        } else {
            return new SVGIcon(uri, newWidth, newHeight);
        }
    }
}