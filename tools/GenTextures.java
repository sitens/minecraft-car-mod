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
        hoesacBoss();
        tackItem();
        tackBlock();
        woodenBucket("wooden_bucket", 0);
        woodenBucket("wooden_water_bucket", 0xFF3F76E4);
        woodenBucket("wooden_lava_bucket", 0xFFE8641A);
        chipBag("dorinos", 0xFFC8202A, 0xFF1A1A1A, 0xFFF08A1E, true);
        chipBag("fritoz", 0xFF2B6FD1, 0xFFF7D02C, 0xFFD9A441, false);
        chipBag("layz", 0xFFF7D02C, 0xFFD8262C, 0xFFF3D78A, false);
        chipBag("layz_bbq", 0xFF7A1F1F, 0xFFF7D02C, 0xFFB5652B, false);
        candyBar();
        soda();
        gummyWorms();
        donut();
        lollipop();
        popcorn();
        System.out.println("textures written");
    }

    // ------------------------------------------------------------- junk food

    static void chipBag(String name, int bag, int stripe, int chip, boolean triangleChip) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int outline = shift(bag, -70);
        for (int y = 2; y <= 13; y++) {
            for (int x = 3; x <= 12; x++) {
                boolean edge = x == 3 || x == 12;
                set(img, x, y, edge ? outline : (x < 6 ? shift(bag, 25) : bag));
            }
        }
        for (int x = 3; x <= 12; x++) {                 // crimped top and bottom
            set(img, x, x % 2 == 0 ? 1 : 2, outline);
            set(img, x, x % 2 == 0 ? 14 : 13, outline);
        }
        rect(img, 4, 4, 8, 2, stripe);                  // brand stripe
        if (triangleChip) {
            for (int row = 0; row < 5; row++) {
                for (int x = 8 - row; x <= 8 + row - 1; x++) {
                    set(img, x, 7 + row, (x + row) % 3 == 0 ? shift(chip, -40) : chip);
                }
            }
        } else {
            for (int y = 7; y <= 11; y++) {
                for (int x = 5; x <= 10; x++) {
                    double dx = x - 7.5, dy = y - 9;
                    if (dx * dx / 7.0 + dy * dy / 5.0 <= 1.0) {
                        set(img, x, y, (x * y) % 5 == 0 ? shift(chip, -35) : chip);
                    }
                }
            }
        }
        save(img, "item/" + name + ".png");
    }

    static void candyBar() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        rect(img, 1, 5, 14, 6, 0xFF6B3A1E);             // chocolate
        rect(img, 1, 5, 14, 1, 0xFF8A5230);
        rect(img, 4, 5, 8, 6, 0xFF2D6CC9);              // blue wrapper in the middle
        rect(img, 5, 7, 6, 2, 0xFFF2F2F2);              // label
        for (int y = 5; y <= 10; y++) {                 // twisted wrapper ends
            set(img, 0, y, y % 2 == 0 ? 0xFFB8B8B8 : 0xFF8A8A8A);
            set(img, 15, y, y % 2 == 0 ? 0xFFB8B8B8 : 0xFF8A8A8A);
        }
        save(img, "item/candy_bar.png");
    }

    static void soda() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        rect(img, 5, 3, 6, 11, 0xFFC8202A);             // can
        rect(img, 5, 3, 1, 11, 0xFFE8505A);             // shine
        rect(img, 10, 3, 1, 11, 0xFF8A1018);            // shadow
        rect(img, 5, 7, 6, 2, 0xFFF2F2F2);              // white wave
        rect(img, 6, 2, 4, 1, 0xFFB8B8B8);              // silver lid
        rect(img, 5, 14, 6, 1, 0xFF9A9A9A);             // silver bottom
        set(img, 8, 1, 0xFF8A8A8A);                     // pull tab
        save(img, "item/soda.png");
    }

    static void gummyWorms() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int[][] colors = {{0xFFFF5A5A, 0xFFFFD84A}, {0xFF4AD26A, 0xFFFF8C3A}, {0xFF5AA0FF, 0xFFFF6AD5}};
        for (int w = 0; w < 3; w++) {
            int baseY = 3 + w * 4;
            for (int x = 2; x <= 13; x++) {
                int y = baseY + (int) Math.round(Math.sin(x * 0.8 + w) * 1.2);
                int c = x < 8 ? colors[w][0] : colors[w][1];
                set(img, x, y, c);
                set(img, x, y + 1, shift(c, -40));
            }
        }
        save(img, "item/gummy_worms.png");
    }

    static void donut() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int[] sprinkles = {0xFFFFFFFF, 0xFF4AD2FF, 0xFFFFE04A, 0xFF7AE07A};
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double d = Math.hypot(x - 7.5, y - 7.5);
                if (d <= 6.8 && d >= 2.2) {
                    boolean icing = d <= 5.6 && y <= 11;
                    int c = icing ? 0xFFF58AC4 : 0xFFD9A05B;
                    if (icing && (x * 7 + y * 3) % 11 == 0) {
                        c = sprinkles[(x + y) % sprinkles.length];
                    }
                    set(img, x, y, c);
                }
            }
        }
        save(img, "item/donut.png");
    }

    static void lollipop() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 9; y <= 15; y++) {                 // stick
            set(img, 8, y, 0xFFF2F2F2);
        }
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double dx = x - 7.5, dy = y - 5.0;
                double d = Math.hypot(dx, dy);
                if (d <= 4.8) {
                    double angle = Math.atan2(dy, dx) + d * 0.9;   // swirl
                    boolean red = Math.sin(angle * 2) > 0;
                    set(img, x, y, red ? 0xFFE0303A : 0xFFFFF2F2);
                }
            }
        }
        save(img, "item/lollipop.png");
    }

    static void popcorn() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Random rnd = new Random(7);
        for (int y = 9; y <= 14; y++) {                 // bowl
            int inset = (y - 9) / 2;
            for (int x = 2 + inset; x <= 13 - inset; x++) {
                set(img, x, y, y == 9 ? 0xFF5A3A1E : 0xFF8A5A30);
            }
        }
        for (int y = 3; y <= 8; y++) {                  // heap of puffy kernels
            int halfWidth = 2 + (y - 3);
            for (int x = 8 - halfWidth; x <= 7 + halfWidth; x++) {
                if (x < 2 || x > 13) {
                    continue;
                }
                int roll = rnd.nextInt(10);
                int c = roll < 2 ? 0xFFF2D36A : roll < 4 ? 0xFFE8DDBF : 0xFFFFFBEA;
                set(img, x, y, c);
            }
        }
        save(img, "item/popcorn.png");
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

    // ------------------------------------------------------------ final boss

    static void hoesacBoss() throws IOException {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Random rnd = new Random(1337);
        int orange = 0xFFE8841A;
        int purple = 0xFF7B2FBE;

        // Everything starts as chunky orange "cheese dust" so any face of any box looks right.
        for (int y = 0; y < 128; y += 2) {
            for (int x = 0; x < 128; x += 2) {
                int c = shift(orange, rnd.nextInt(41) - 20);
                rect(img, x, y, 2, 2, c);
            }
        }
        // Purple shirt stripes across the belly (body sides at v 56..74, front at x 26..52).
        for (int y = 56; y < 74; y += 6) {
            for (int x = 0; x < 104; x += 2) {
                rect(img, x, y, 2, 3, shift(purple, rnd.nextInt(25) - 12));
            }
        }
        // Crumbs and stains scattered on the body.
        for (int i = 0; i < 70; i++) {
            set(img, rnd.nextInt(104), 30 + rnd.nextInt(44), i % 3 == 0 ? 0xFFF7D02C : 0xFFB5501A);
        }
        // A big cheesy triangle on the belly front.
        for (int row = 0; row < 10; row++) {
            for (int x = 39 - row; x <= 39 + row; x++) {
                set(img, x, 58 + row, row % 3 == 0 ? 0xFFF7D02C : 0xFFF2A21E);
            }
        }

        // Head front is (12,12)-(24,21): a big grumpy face.
        rect(img, 13, 14, 3, 1, 0xFF3A1A00);              // angry eyebrows
        rect(img, 20, 14, 3, 1, 0xFF3A1A00);
        rect(img, 13, 15, 3, 3, 0xFFFFFFFF);              // eyes
        rect(img, 20, 15, 3, 3, 0xFFFFFFFF);
        rect(img, 14, 16, 2, 2, 0xFFC8202A);              // red pupils
        rect(img, 20, 16, 2, 2, 0xFFC8202A);
        rect(img, 14, 18, 8, 3, 0xFF5A0F0F);              // huge open mouth
        for (int x = 14; x < 22; x += 2) {                // teeth
            set(img, x, 18, 0xFFFFFFFF);
            set(img, x + 1, 20, 0xFFFFFFFF);
        }
        save(img, "entity/hoesac_final_boss.png");
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
