import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Draws the mod's placeholder textures (all original pixel art, nothing
 * copied from Minecraft). Run from the mod folder with:
 *
 *   java tools/GenTextures.java
 *
 * and it (re)writes the PNGs under src/main/resources/assets/carmod/textures.
 */
public class GenTextures {

    static final String ROOT = "src/main/resources/assets/carmod/textures/";

    public static void main(String[] args) throws IOException {
        hoesac();
        tackItem();
        tackBlock();
        woodenBucket("wooden_bucket", 0);
        woodenBucket("wooden_water_bucket", 0xFF3F76E4);
        woodenBucket("wooden_lava_bucket", 0xFFE8641A);
        System.out.println("textures written");
    }

    // ---------------------------------------------------------------- hOesaC

    static void hoesac() throws IOException {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Random rnd = new Random(42);
        int skin = 0xFFE8841A;   // cheese-dust orange
        int shirt = 0xFF7B2FBE;  // purple
        int pants = 0xFF26306B;  // dark blue

        fillNoisy(img, rnd, 0, 0, 32, 16, skin, 18);      // head (hat layer at 32,0 stays see-through)
        fillNoisy(img, rnd, 16, 16, 24, 16, shirt, 12);   // body
        fillNoisy(img, rnd, 40, 16, 16, 16, skin, 18);    // arms
        fillNoisy(img, rnd, 40, 20, 16, 3, shirt, 12);    // short sleeves
        fillNoisy(img, rnd, 0, 16, 16, 16, pants, 10);    // legs

        // Crumbs on the shirt front
        for (int i = 0; i < 10; i++) {
            set(img, 20 + rnd.nextInt(8), 20 + rnd.nextInt(12), 0xFFF2C14E);
        }
        // Face on the front of the head (8,8)-(16,16)
        rect(img, 9, 10, 2, 1, 0xFF3A1A00);   // eyebrows, grumpy
        rect(img, 13, 10, 2, 1, 0xFF3A1A00);
        rect(img, 9, 11, 2, 2, 0xFFFFFFFF);   // eyes
        rect(img, 13, 11, 2, 2, 0xFFFFFFFF);
        set(img, 10, 12, 0xFF000000);
        set(img, 13, 12, 0xFF000000);
        rect(img, 10, 14, 4, 2, 0xFF5A0F0F);  // big open mouth
        set(img, 10, 14, 0xFFFFFFFF);         // teeth
        set(img, 13, 14, 0xFFFFFFFF);
        save(img, "entity/hoesac.png");
    }

    // ------------------------------------------------------------------ tack

    static void tackItem() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        // round head
        for (int y = 1; y <= 6; y++) {
            for (int x = 4; x <= 11; x++) {
                double dx = x - 7.5, dy = y - 3.5;
                if (dx * dx / 16.0 + dy * dy / 7.0 <= 1.0) {
                    set(img, x, y, dy < 0 ? 0xFFE0E0E0 : 0xFFA8A8A8);
                }
            }
        }
        rect(img, 5, 7, 6, 1, 0xFF6E6E6E);           // neck
        for (int y = 8; y <= 14; y++) {              // pin, tapering to a point
            set(img, 7, y, 0xFFC8C8C8);
            if (y < 13) {
                set(img, 8, y, 0xFF8A8A8A);
            }
        }
        set(img, 7, 15, 0xFF5A5A5A);
        save(img, "item/tack.png");
    }

    static void tackBlock() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int shade = 150 + (x % 4 == 0 ? 40 : 0) - y * 2;
                set(img, x, y, 0xFF000000 | (shade << 16) | (shade << 8) | shade);
            }
        }
        save(img, "block/tack.png");
    }

    // --------------------------------------------------------- wooden bucket

    static void woodenBucket(String name, int contents) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int outline = 0xFF3B2410;
        int wood = 0xFF9C6B30;
        int woodDark = 0xFF7A5226;
        // tapered body: wide at the top (y=4), narrow at the bottom (y=14)
        for (int y = 4; y <= 14; y++) {
            int inset = (y - 4) / 4;
            int left = 2 + inset;
            int right = 13 - inset;
            for (int x = left; x <= right; x++) {
                boolean edge = x == left || x == right || y == 14;
                int plank = ((x - left) % 3 == 0) ? woodDark : wood;
                set(img, x, y, edge ? outline : plank);
            }
        }
        rect(img, 2, 7, 12, 1, 0xFF6B6B6B);   // iron band
        rect(img, 3, 11, 10, 1, 0xFF6B6B6B);
        // rim / opening
        rect(img, 2, 3, 12, 1, outline);
        rect(img, 3, 4, 10, 1, contents != 0 ? contents : 0xFF2A1A0C);
        if (contents != 0) {
            set(img, 5, 4, lighten(contents));
            set(img, 9, 4, lighten(contents));
        }
        // handle
        for (int x = 3; x <= 12; x++) {
            int y = x < 5 || x > 10 ? 2 : 1;
            set(img, x, y, 0xFF8A8A8A);
        }
        save(img, "item/" + name + ".png");
    }

    // --------------------------------------------------------------- helpers

    static void fillNoisy(BufferedImage img, Random rnd, int x0, int y0, int w, int h, int color, int noise) {
        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                int d = rnd.nextInt(noise * 2 + 1) - noise;
                set(img, x, y, shift(color, d));
            }
        }
    }

    static void rect(BufferedImage img, int x0, int y0, int w, int h, int color) {
        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                set(img, x, y, color);
            }
        }
    }

    static void set(BufferedImage img, int x, int y, int argb) {
        img.setRGB(x, y, argb);
    }

    static int shift(int argb, int d) {
        int r = clamp(((argb >> 16) & 0xFF) + d);
        int g = clamp(((argb >> 8) & 0xFF) + d);
        int b = clamp((argb & 0xFF) + d);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    static int lighten(int argb) {
        return shift(argb, 60);
    }

    static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    static void save(BufferedImage img, String path) throws IOException {
        File out = new File(ROOT + path);
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
    }
}
