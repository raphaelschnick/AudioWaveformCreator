package main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Audio Waveform Creator \n\n");

        AudioWaveformCreator audioWaveformCreator = new AudioWaveformCreator();

        while (true) {
            System.out.println("[store/1.wav] \n[store/2.wav]");
            System.out.println("File Path: ");
            Scanner scanner = new Scanner(System.in);
            File file = new File(scanner.nextLine());
            if (file.exists()) {
                File result = audioWaveformCreator.createWaveForm(file);
                System.out.println("Successfully created Audio Waveform \n" + result.getAbsolutePath());
            } else {
                System.out.println("File " + file.getAbsolutePath() + " not Found!");
            }
        }
    }
}
