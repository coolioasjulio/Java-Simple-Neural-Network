package com.coolioasjulio.neuralnetwork.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("unused")
public class ImageUtils {
	public static void main(String[] args) throws IOException{
		BufferedImage[] images = getImages("data/t10k-images.idx3-ubyte");
		BufferedImage image = new BufferedImage(28, 28, BufferedImage.TYPE_INT_ARGB);
		double[] data = getDataFromBufferedImage(images[0]);
		int index = 0;
		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				int val = Math.round((float)(data[index]*255d));
				int rgb = new Color(val, val, val).getRGB();
				image.setRGB(x, y, rgb);
				index++;
			}
		}
		showImage(images[0]);
		showImage(image);
	}
	
	public static double[][] getLabels(String path) throws IOException{
		try(DataInputStream in = new DataInputStream(new FileInputStream(path))){
			in.readInt();
			int numLabels = in.readInt();
			//numLabels = 100;
			double[][] labels = new double[numLabels][10];
			for(int i = 0; i < labels.length; i++){
				int val = in.read();
				labels[i][val%10] = 1;
			}
			return labels;
		}
		catch(IOException e){
			throw e;
		}
	}
	
	public static BufferedImage[] getImages(String path) throws IOException{
		try(DataInputStream in = new DataInputStream(new FileInputStream(path))){
			in.readInt();
			int numImages = in.readInt();
			//numImages = 100;
			int rows = in.readInt();
			int cols = in.readInt();
			
			BufferedImage[] images = new BufferedImage[numImages];
			
			for(int i = 0; i < images.length; i++){
				BufferedImage image = new BufferedImage(rows,cols,BufferedImage.TYPE_INT_RGB);
				for(int x = 0; x < rows; x++){
					for(int y = 0; y < cols; y++){
						int val = in.read();
						image.setRGB(y, x, new Color(val, val, val).getRGB());
					}
				}
				images[i] = image;
			}
			
			return images;
		} catch (IOException e) {
			throw e;
		}
	}
	
	public static void monoColor(BufferedImage img){
		for(int i = 0; i < img.getWidth(); i++){
			for(int j = 0; j < img.getHeight(); j++){
				Color c = new Color(img.getRGB(i, j));
				int avg = (c.getRed() + c.getGreen() + c.getBlue())/3;
				Color mono = new Color(avg, avg, avg);
				img.setRGB(i,j, mono.getRGB());
			}
		}
	}
	
	public static void showImage(BufferedImage image){
		JFrame frame = new JFrame();
		frame.setSize(280, 280);
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(image.getScaledInstance(280, 280, BufferedImage.SCALE_FAST)));
		frame.getContentPane().add(label);
		frame.setVisible(true);
	}
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	private static double white = -1;
	private static double black = -1;
	public static double[] getDataFromBufferedImage(BufferedImage img){
		if(white == -1) white = Color.WHITE.getRGB();
		if(black == -1) black = Color.BLACK.getRGB();
		double[] toReturn = new double[img.getWidth()*img.getHeight()];
		int index = 0;
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				toReturn[index] = grayScale(new Color(img.getRGB(x, y)));
				index++;
			}
		}
		return otsu(toReturn);
	}
	
	private static double[] normalize(double[] original){
		double[] normalized = new double[original.length];
		for(int i = 0; i < original.length; i++){
			double rgb = original[i];
			normalized[i] = rgb/255;
		}
		System.out.println(Arrays.toString(normalized));
		return normalized;
	}
	
	private static double[] otsu(double[] original){
		double[] data = original.clone();
		int[] histogram = new int[256];
        for(double datum : data) {
            histogram[(int) datum]++;
        }

        double sum = 0;
        for(int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        int wF = 0;
        double maxVariance = 0;
        int threshold = 0;
        int i = 0;
        boolean found = false;

        while(i < histogram.length && !found) {
            wB += histogram[i];
            if(wB != 0) {
                wF = data.length - wB;
                if(wF != 0) {
                    sumB += (i * histogram[i]);
                    double mB = sumB / wB;
                    double mF = (sum - sumB) / wF;
                    double varianceBetween = wB * Math.pow((mB - mF), 2);

                    if(varianceBetween > maxVariance) {
                        maxVariance = varianceBetween;
                        threshold = i;
                    }
                }
                else {
                    found = true;
                }
            }
            i++;
        }
        
        for(i = 0; i < data.length; i++) {
            data[i] = data[i] <= threshold ? 0 : 1;
        }
        
        return data;
	}
	
	private static double grayScale(Color color){
		return (color.getRed() + color.getBlue() + color.getGreen()) / 3;
	}
	
	public static double[] getCondensedData(BufferedImage img){
		Area a = new Area();
		int numPixels = 0;
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				if(!new Color(img.getRGB(x, y)).equals(Color.BLACK)){
					numPixels++;
					a.add(new Area(new Rectangle(x,y,1,1)));
				}
			}
		}
		Rectangle rect = a.getBounds();
		double[] toReturn = new double[5];
		toReturn[0] = rect.getCenterX()/(double)img.getWidth();;
		toReturn[1] = rect.getCenterY()/(double)img.getHeight();
		toReturn[2] = rect.getWidth()/(double)img.getWidth();
		toReturn[3] = rect.getHeight()/(double)img.getHeight();
		toReturn[4] = numPixels/(double)(img.getHeight()*img.getWidth());
		return toReturn;
	}
}