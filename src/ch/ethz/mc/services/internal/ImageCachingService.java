package ch.ethz.mc.services.internal;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.RandomStringUtils;

import ch.ethz.mc.conf.ImplementationConstants;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;

/**
 * Image service to store, cache, manipulate and return (modified) images
 *
 * @author Andreas Filler
 */
@Log4j2
public class ImageCachingService {
	private static ImageCachingService	instance;

	private final File					mediaCacheFolder;

	private final float					jpegCompression;

	private final Font					watermarkFont;
	private final String				watermarkText;

	private final HashMap<String, File>	imageCacheMap;

	private ImageCachingService(final File mediaCacheFolder) {
		watermarkFont = new Font("Arial", Font.BOLD, 30);

		this.mediaCacheFolder = mediaCacheFolder;

		jpegCompression = ImplementationConstants.IMAGE_JPEG_COMPRESSION;
		watermarkText = ImplementationConstants.IMAGE_WATERMARK_TEXT;

		imageCacheMap = new HashMap<String, File>();

		log.info("JPEG compression: " + jpegCompression);
		log.info("Watermark text: " + watermarkText);

		log.info("Started");
	}

	public static ImageCachingService start(final File mediaCacheFolder)
			throws Exception {
		log.info("Starting service...");
		if (instance == null) {
			instance = new ImageCachingService(mediaCacheFolder);
		}
		log.info("Started.");
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/**
	 * Requests a specific image in a specific size
	 *
	 * @param sourceImageFile
	 * @param width
	 * @param height
	 * @param withWatermark
	 * @return
	 */
	public File requestCacheImage(final File sourceImageFile, final int width,
			final int height, final boolean withWatermark,
			final boolean withCropping) {
		final String key = sourceImageFile.getAbsoluteFile().getParentFile()
				.getName()
				+ "-"
				+ width
				+ "-"
				+ height
				+ "-"
				+ withWatermark
				+ "-"
				+ withCropping;

		log.debug("Requesting image with key '{}'", key);

		synchronized (imageCacheMap) {
			if (imageCacheMap.containsKey(key)) {
				final File existingImageFile = imageCacheMap.get(key);

				if (existingImageFile.exists()) {
					log.debug("Returning image from cache: {}"
							+ existingImageFile.getAbsolutePath());
					return imageCacheMap.get(key);
				}
			}
		}

		File cacheFile = null;
		try {
			cacheFile = createCacheImage(sourceImageFile, width, height, key,
					withWatermark, withCropping);
		} catch (final Exception e) {
			log.warn("Could not create image {} with {}*{} watermark: {}",
					sourceImageFile.getAbsoluteFile(), width, height,
					withWatermark);
		}

		if (cacheFile != null && cacheFile.exists()) {
			imageCacheMap.put(key, cacheFile);
		}

		return cacheFile;
	}

	/**
	 * Creates a new cached image
	 *
	 * @param sourceImageFile
	 * @param width
	 * @param height
	 * @param key
	 * @param withWatermark
	 * @return
	 */
	private File createCacheImage(final File sourceImageFile, final int width,
			final int height, final String key, final boolean withWatermark,
			final boolean withCropping) {
		File targetImageFile = null;
		final String filename = ImplementationConstants.FILE_STORAGE_PREFIX
				+ RandomStringUtils.randomAlphanumeric(40) + "-" + key + ".jpg";

		try {
			BufferedImage readImage = ImageIO.read(sourceImageFile);

			if (readImage.getColorModel().getTransparency() != Transparency.OPAQUE) {
				readImage = fillTransparentPixels(readImage, Color.WHITE);
			}

			final int w = readImage.getWidth();
			final int h = readImage.getHeight();
			final BufferedImage image = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);

			final int[] rgb = readImage.getRGB(0, 0, w, h, null, 0, w);
			image.setRGB(0, 0, w, h, rgb, 0, w);

			targetImageFile = new File(mediaCacheFolder, filename);
			resizeAndWriteFile(image, width, height, withWatermark,
					targetImageFile, withCropping);
		} catch (final Exception e) {
			log.warn("Can't perform image transformation: {}", e.getMessage());
		}

		return targetImageFile;
	}

	/**
	 * Fills transparent pixels for PNG/JPG compatibility
	 *
	 * @param image
	 * @param fillColor
	 * @return
	 */
	private BufferedImage fillTransparentPixels(final BufferedImage image,
			final Color fillColor) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final BufferedImage newImage = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = newImage.createGraphics();
		g.setColor(fillColor);
		g.fillRect(0, 0, w, h);
		g.drawRenderedImage(image, null);
		g.dispose();
		return newImage;
	}

	/**
	 * Resizes images and write it as file
	 *
	 * @param image
	 * @param width
	 * @param height
	 * @param withWatermark
	 * @param outputFile
	 * @param withCropping
	 * @throws Exception
	 */
	private void resizeAndWriteFile(BufferedImage image, final int width,
			final int height, final boolean withWatermark,
			final File outputFile, final boolean withCropping) throws Exception {
		log.debug("Resizing image...");

		// Resize (maintaining ratio)
		final double widthRatio = (double) width / image.getWidth();
		final double heightRatio = (double) height / image.getHeight();
		final double scale;
		if (withCropping) {
			scale = Math.max(widthRatio, heightRatio);
		} else {
			scale = Math.min(widthRatio, heightRatio);
		}

		final int resizeWidth = (int) (scale * image.getWidth());
		final int resizeHeight = (int) (scale * image.getHeight());

		final MultiStepRescaleOp rescale = new MultiStepRescaleOp(resizeWidth,
				resizeHeight);
		rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
		image = rescale.filter(image, null);

		// Crop
		if (withCropping) {
			if (resizeWidth > width || resizeHeight > height) {
				final int x1 = 0 + (resizeWidth - width) / 2;
				final int y1 = 0 + (resizeHeight - height) / 2;
				final int x2 = resizeWidth - (resizeWidth - width) / 2;
				final int y2 = resizeHeight - (resizeHeight - height) / 2;

				image = crop(image, x1, y1, x2, y2, width, height);
			}
		}

		// Watermark
		if (withWatermark) {
			final Graphics2D g2d = (Graphics2D) image.getGraphics();
			// g2d.drawImage(icon.getImage(), 0, 0, null);
			final AlphaComposite alpha = AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f);
			g2d.setComposite(alpha);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setFont(watermarkFont);
			g2d.setColor(Color.black);
			final FontMetrics fontMetrics = g2d.getFontMetrics();
			final Rectangle2D rect = fontMetrics.getStringBounds(watermarkText,
					g2d);
			g2d.drawString(watermarkText,
					image.getWidth() - (int) rect.getWidth() - 20,
					image.getHeight() - 20);
			g2d.setColor(Color.white);
			g2d.drawString(watermarkText,
					image.getWidth() - (int) rect.getWidth() - 17,
					image.getHeight() - 17);
			g2d.dispose();
		}

		// Save
		writeToJPG(image, outputFile, jpegCompression);
	}

	/**
	 * Crops image
	 *
	 * @param image
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	private BufferedImage crop(final BufferedImage image, final int x1,
			final int y1, final int x2, final int y2, final int newWidth,
			final int newHeight) {
		final int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
				: image.getType();
		final BufferedImage cropped = new BufferedImage(newWidth, newHeight,
				type);
		final Graphics2D g = cropped.createGraphics();

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				ImplementationConstants.IMAGE_JPEG_RENDERING);
		g.setComposite(AlphaComposite.Src);

		g.drawImage(image, 0, 0, newWidth, newHeight, x1, y1, x2, y2, null);
		g.dispose();

		return cropped;
	}

	/**
	 * Writes immage to JPEG file
	 *
	 * @param image
	 * @param outputFile
	 * @param quality
	 * @throws Exception
	 */
	private void writeToJPG(final BufferedImage image, final File outputFile,
			final float quality) throws Exception {
		log.debug("Writing image...");

		// Encodes image as a JPEG data stream
		final ImageWriter jpgWriter = ImageIO
				.getImageWritersByFormatName("jpg").next();
		final ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(quality);

		final FileImageOutputStream outputStream = new FileImageOutputStream(
				outputFile);

		jpgWriter.setOutput(outputStream);
		final IIOImage outputImage = new IIOImage(image, null, null);
		final IIOMetadata metaData = jpgWriter.getDefaultImageMetadata(
				new ImageTypeSpecifier(image), jpgWriteParam);
		jpgWriter.write(metaData, outputImage, jpgWriteParam);
		jpgWriter.dispose();
	}
}
