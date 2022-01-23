package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.InputMismatchException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class AudioWaveformCreator {

    private AudioInputStream audioInputStream;
    private final Vector<Line2D.Double> lines = new Vector<Line2D.Double>();
    private double duration;
    private final Font font12 = new Font("serif", Font.PLAIN, 12);
    private final Color secondary = new Color(0, 0, 0);

    public File createWaveForm(File file) throws IOException {
        byte[] audioBytes = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
            duration = milliseconds / 1000.0;
        } catch (Exception ex) {
           throw new InputMismatchException(ex.getMessage());
        }

        lines.removeAllElements();

        AudioFormat format = audioInputStream.getFormat();
        try {
            audioBytes = new byte[
                    (int) (audioInputStream.getFrameLength()
                            * format.getFrameSize())];
            audioInputStream.read(audioBytes);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
        int w = 500;
        int h = 200;
        int[] audioData = null;
        if (format.getSampleSizeInBits() == 16) {
            int nlengthInSamples = audioBytes.length / 2;
            audioData = new int[nlengthInSamples];
            for (int i = 0; i < nlengthInSamples; i++) {
                int MSB = (int) audioBytes[2 * i];
                int LSB = (int) audioBytes[2 * i + 1];
                audioData[i] = MSB << 8 | (255 & LSB);
            }
        } else if (format.getSampleSizeInBits() == 8) {
            int nlengthInSamples = audioBytes.length;
            audioData = new int[nlengthInSamples];
            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                for (int i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i];
                }
            } else {
                for (int i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i] - 128;
                }
            }
        }

        int frames_per_pixel = audioBytes.length / format.getFrameSize() / w;
        byte my_byte = 0;
        double y_last = 0;
        int numChannels = format.getChannels();
        for (double x = 0; x < w && audioData != null; x++) {
            int idx = (int) (frames_per_pixel * numChannels * x);
            if (format.getSampleSizeInBits() == 8) {
                my_byte = (byte) audioData[idx];
            } else {
                my_byte = (byte) (128 * audioData[idx] / 32768);
            }
            double y_new = (double) (h * (128 - my_byte) / 256);
            lines.add(new Line2D.Double(x, y_last, x, y_new));
            y_last = y_new;
        }
        File result = new File("result/" + file.getName() + ".png");
        if (result.createNewFile()) {
            saveToFile(result);
            return result;
        } else {
            throw new FileAlreadyExistsException("File already Exists " + result.getAbsolutePath());
        }
    }


    public void saveToFile(File file) {
        var w = 500;
        var h = 200;
        var infoPad = 1000;

        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();

        createSampleOnGraphicsContext(w, h, infoPad, g2);
        g2.dispose();
        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    private void createSampleOnGraphicsContext(int w, int h, int infoPad, Graphics2D g2) {
        g2.setBackground(secondary);
        g2.clearRect(0, 0, w, h);
        g2.fillRect(0, h - infoPad, w, infoPad);
        g2.setFont(font12);

        if (audioInputStream != null) {
            g2.setColor(secondary);
            for (var i = 1; i < lines.size(); i++) {
                g2.draw(lines.get(i));
            }
        }
    }
}
