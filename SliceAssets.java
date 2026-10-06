import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.imageio.ImageIO;

public class SliceAssets {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("asset.png"));
        int w = img.getWidth();
        int h = img.getHeight();

        int cellW = w / 3; // 898
        int cellH = h / 2; // 784

        File outDir = new File("assets");
        if (!outDir.exists()) outDir.mkdirs();

        String[][] names = {
            {"nha", "bai_do_xe", "vao_tu"},
            {"bat_dau", "co_hoi", "thue"}
        };

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int x0 = col * cellW;
                int y0 = row * cellH;
                String name = names[row][col];

                // The isometric graphic sits between y0 and y0 + 0.78 * cellH
                // Cut off the text label underneath (y > 0.78 * cellH)
                int cutoffY = y0 + (int)(cellH * 0.77);
                if (name.equals("co_hoi")) cutoffY = y0 + (int)(cellH * 0.74);
                if (name.equals("nha")) cutoffY = y0 + (int)(cellH * 0.76);
                if (name.equals("bat_dau")) cutoffY = y0 + (int)(cellH * 0.75);

                // Step 1: Flood-fill background starting from the border of (x0..x0+cellW, y0..cutoffY)
                int subW = cellW;
                int subH = cutoffY - y0;
                boolean[][] isBg = new boolean[subW][subH];
                Queue<int[]> q = new ArrayDeque<>();

                // Seed top, left, right edges, and bottom corners
                for (int x = 0; x < subW; x++) {
                    q.add(new int[]{x, 0});
                    isBg[x][0] = true;
                    // Also bottom edges (left and right of the pedestal)
                    if (x < subW * 0.15 || x > subW * 0.85) {
                        q.add(new int[]{x, subH - 1});
                        isBg[x][subH - 1] = true;
                    }
                }
                for (int y = 0; y < subH; y++) {
                    q.add(new int[]{0, y});
                    isBg[0][y] = true;
                    q.add(new int[]{subW - 1, y});
                    isBg[subW - 1][y] = true;
                }

                int[] dx = {1, -1, 0, 0};
                int[] dy = {0, 0, 1, -1};

                while (!q.isEmpty()) {
                    int[] p = q.poll();
                    int cx = p[0], cy = p[1];
                    for (int d = 0; d < 4; d++) {
                        int nx = cx + dx[d], ny = cy + dy[d];
                        if (nx >= 0 && nx < subW && ny >= 0 && ny < subH && !isBg[nx][ny]) {
                            int rgb = img.getRGB(x0 + nx, y0 + ny);
                            int r = (rgb >> 16) & 0xff;
                            int g = (rgb >> 8) & 0xff;
                            int b = rgb & 0xff;
                            // Check if dark background
                            // The background is around (27, 30, 37) with possible glows up to (52, 52, 58)
                            if (r <= 48 && g <= 50 && b <= 56) {
                                isBg[nx][ny] = true;
                                q.add(new int[]{nx, ny});
                            }
                        }
                    }
                }

                // Step 2: Find tight bounding box of non-background pixels
                int minX = subW, maxX = 0;
                int minY = subH, maxY = 0;
                for (int y = 0; y < subH; y++) {
                    for (int x = 0; x < subW; x++) {
                        if (!isBg[x][y]) {
                            if (x < minX) minX = x;
                            if (x > maxX) maxX = x;
                            if (y < minY) minY = y;
                            if (y > maxY) maxY = y;
                        }
                    }
                }

                int pad = 4;
                minX = Math.max(0, minX - pad);
                minY = Math.max(0, minY - pad);
                maxX = Math.min(subW - 1, maxX + pad);
                maxY = Math.min(subH - 1, maxY + pad);

                int cropW = maxX - minX + 1;
                int cropH = maxY - minY + 1;

                BufferedImage outImg = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
                for (int cy = 0; cy < cropH; cy++) {
                    for (int cx = 0; cx < cropW; cx++) {
                        int sx = minX + cx;
                        int sy = minY + cy;
                        if (isBg[sx][sy]) {
                            outImg.setRGB(cx, cy, 0x00000000);
                        } else {
                            int rgb = img.getRGB(x0 + sx, y0 + sy);
                            int r = (rgb >> 16) & 0xff;
                            int g = (rgb >> 8) & 0xff;
                            int b = rgb & 0xff;
                            // Soft edge anti-aliasing if near background threshold
                            if (r <= 52 && g <= 54 && b <= 60) {
                                float t = Math.max(r - 27, Math.max(g - 30, b - 37)) / 25.0f;
                                int a = Math.min(255, Math.max(0, (int)(t * 255)));
                                outImg.setRGB(cx, cy, (a << 24) | (r << 16) | (g << 8) | b);
                            } else {
                                outImg.setRGB(cx, cy, (0xff << 24) | (rgb & 0x00ffffff));
                            }
                        }
                    }
                }

                File outFile = new File(outDir, name + ".png");
                ImageIO.write(outImg, "png", outFile);
                System.out.println("Cleanly saved: " + outFile.getAbsolutePath() + " (" + cropW + "x" + cropH + ")");
            }
        }
    }
}
