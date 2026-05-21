package dev.hero.test.nyxcore.discord.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

@Service
@Slf4j
public class ImageService {

    // Regex to find ANSI codes
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[0-9;]*m");

    /**
     * Loads the custom font (JetBrains Mono) from resources.
     * Falls back to standard "Monospaced" if the file is missing.
     */
    private static Font loadCustomFont() {
        try {
            // Ensure this path matches your src/main/resources folder structure exactly!
            InputStream is = ImageService.class.getResourceAsStream("/fonts/JetBrainsMono-Medium.ttf");

            if (is == null) {
                log.warn("Font file not found in resources. Falling back to Monospaced.");
                return new Font("Monospaced", Font.BOLD, 20);
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

            // Return at size 20 (Bold)
            return customFont.deriveFont(Font.PLAIN, 20f);

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("❌ Error loading font. Falling back to Monospaced.");
            return new Font("Monospaced", Font.BOLD, 20);
        }
    }

    public static InputStream createTerminalImage(String text) {
        try {
            // 1. FONT SETUP: Call the loader!
            // DO NOT use "Arial" or "Dialog". Only Monospaced fonts align tables.
            Font font = loadCustomFont();

            String[] lines = text.split("\n");

            // 2. MEASURE WIDTH
            BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            Graphics2D gDummy = dummy.createGraphics();
            gDummy.setFont(font);
            FontMetrics fm = gDummy.getFontMetrics();
            int lineHeight = fm.getHeight();

            // For a Monospaced font, 'W' and 'i' have the same width.
            // If you use Arial, this calculation breaks.
            int charWidth = fm.charWidth('W');

            int maxCharCount = 0;
            for (String line : lines) {
                // REPAIR STEP: Fix broken dysk codes
                if (line.contains("[38;5;")) {
                    line = line.replace("[38;5;", "\u001B[38;5;");
                    line = line.replace("\u001B\u001B", "\u001B");
                }

                String clean = ANSI_PATTERN.matcher(line).replaceAll("");
                maxCharCount = Math.max(maxCharCount, clean.length());
            }

            // Dimensions
            int width = (maxCharCount * charWidth) + 80;
            int height = (lines.length * lineHeight) + 80;

            // 3. DRAW
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // High Quality Text Rendering
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Dark Background
            g.setColor(new Color(43, 45, 49));
            g.fillRect(0, 0, width, height);

            g.setFont(font);
            int startX = 40;
            int y = 50;

            for (String line : lines) {
                // REPAIR STEP (Repeat for drawing phase)
                if (line.contains("[38;5;")) {
                    line = line.replace("[38;5;", "\u001B[38;5;");
                    line = line.replace("\u001B\u001B", "\u001B");
                }

                int x = startX;
                Matcher matcher = ANSI_PATTERN.matcher(line);
                int lastEnd = 0;

                // Default Color
                g.setColor(new Color(220, 221, 222));

                while (matcher.find()) {
                    // Draw Text
                    String segment = line.substring(lastEnd, matcher.start());
                    if (!segment.isEmpty()) {
                        g.drawString(segment, x, y);
                        x += fm.stringWidth(segment);
                    }

                    // Apply Color
                    String code = matcher.group();
                    applyColor(g, code);

                    lastEnd = matcher.end();
                }

                // Draw Tail
                String tail = line.substring(lastEnd);
                if (!tail.isEmpty()) {
                    g.drawString(tail, x, y);
                }

                y += lineHeight;
            }
            g.dispose();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void applyColor(Graphics2D g, String code) {
        Color color = new Color(220, 221, 222); // Default Grey

        // --- 1. Standard ANSI Colors (The basics) ---
        if      (code.contains("[31m")) color = new Color(231, 76, 60);       // Red
        else if (code.contains("[32m")) color = new Color(46, 204, 113);      // Green
        else if (code.contains("[33m")) color = new Color(241, 196, 15);      // Yellow
        else if (code.contains("[34m")) color = new Color(52, 152, 219);      // Blue
        else if (code.contains("[35m")) color = new Color(155, 89, 182);      // Purple
        else if (code.contains("[36m")) color = new Color(26, 188, 156);      // Cyan

        // --- 2. Dysk / Fastfetch Specific Codes (Manual Overrides) ---
        // I have manually brightened these values for you below:

        else if (code.contains("38;5;239")) color = new Color(170, 170, 170); // Grey (Brightened from 110)
        else if (code.contains("38;5;209")) color = new Color(255, 100, 80);  // Salmon/Red (Brightened)
        else if (code.contains("38;5;65"))  color = new Color(80, 255, 140);  // Green (Brightened)
        else if (code.contains("38;5;172")) color = new Color(255, 215, 50);  // Gold (Brightened)
        else if (code.contains("38;5;124")) color = new Color(255, 80, 80);   // Deep Red (Significantly Brightened)

        // --- 3. Apply the Color ---
        g.setColor(color);
    }
}