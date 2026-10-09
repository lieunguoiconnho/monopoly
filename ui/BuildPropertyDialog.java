package ProjectOop.ui;

import ProjectOop.player.Player;
import ProjectOop.square.PropertySquare;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * BuildPropertyDialog – Giao diện pop-up Xây Dựng mô phỏng phong cách Cờ Tỷ Phú (ZingPlay/360mobi).
 * Quy tắc:
 *   - Mỗi lần dừng chân chỉ được mua/nâng cấp thêm ĐÚNG 1 CẤP.
 *   - Nếu ô trống: mua bắt đầu từ Mức 1 (Đất nền).
 *   - Nếu đã có nhà của mình: nâng cấp lên mức tiếp theo (+1 cấp), tối đa 4 cấp (Khách sạn).
 */
public class BuildPropertyDialog extends JDialog {

    private final PropertySquare property;
    private final Player player;
    private final boolean isUpgradeOnly;

    // Cấp hiện tại (0: trống, 1: đất nền, 2: nhà phố, 3: chung cư, 4: khách sạn)
    private final int currentLevel;
    // Cấp duy nhất được mua/nâng cấp ở lượt này (targetLevel = currentLevel + 1)
    private final int nextLevelIndex; // 0: Đất nền, 1: Nhà phố, 2: Chung cư, 3: Khách sạn

    private boolean isNextLevelSelected = true;

    private final int[] costs = new int[4];
    private final int[] rents = new int[4];

    private boolean confirmed = false;
    private int finalTargetLevel = 0;
    private int finalTotalCost = 0;

    // UI Components
    private JLabel totalCostLabel;
    private JLabel rentLabel;
    private JLabel btnCostLabel;
    private JLabel lblMuaText;
    private JPanel buyButton;
    private final CardPanel[] cardPanels = new CardPanel[4];

    public BuildPropertyDialog(JFrame parent, Player player, PropertySquare property, boolean isUpgradeOnly) {
        super(parent, "Xây Dựng", true);
        this.player = player;
        this.property = property;
        this.isUpgradeOnly = isUpgradeOnly;

        if (property.getOwner() == player) {
            this.currentLevel = Math.max(1, Math.min(4, property.getHouseLevel()));
        } else {
            this.currentLevel = 0; // Ô trống
        }

        // Cấp tiếp theo được phép nâng (0..3 tương ứng cấp 1..4)
        this.nextLevelIndex = Math.min(3, this.currentLevel);

        setUndecorated(true);
        setSize(700, 440);
        setLocationRelativeTo(parent);

        initPricing();
        buildUI();
    }

    private void initPricing() {
        int houseCost = property.getHouseCost() > 0 ? property.getHouseCost() : property.getPrice() / 2;
        if (houseCost <= 0) houseCost = 50;

        // Chi phí từng cấp (0: Đất nền, 1: Nhà phố, 2: Chung cư, 3: Khách sạn)
        costs[0] = property.getPrice();
        costs[1] = houseCost;
        costs[2] = houseCost;
        costs[3] = houseCost;

        // Phí tham quan từng cấp (1..4)
        rents[0] = property.getRentForLevel(1);
        rents[1] = property.getRentForLevel(2);
        rents[2] = property.getRentForLevel(3);
        rents[3] = property.getRentForLevel(4);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền chính màu kem ấm áp
                g2.setColor(new Color(245, 242, 234));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Viền ngoài thanh lịch
                g2.setColor(new Color(210, 202, 185));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 1. HEADER (Thanh màu xanh lam với tên ô đất)
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 160, 235), 0, getHeight(), new Color(45, 195, 250));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 24, 24);
                g2.fillRect(0, getHeight() - 10, getWidth(), 10);

                g2.setColor(new Color(255, 255, 255, 90));
                g2.drawLine(16, 1, getWidth() - 16, 1);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 58));

        // Tiêu đề tên ô đất
        JLabel titleLabel = new JLabel(property.getName().toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(getText());
                int x = (getWidth() - textW) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(new Color(0, 75, 140, 140));
                g2.drawString(getText(), x, y + 2);

                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Nút Đóng [X] tròn đỏ viền trắng
        JPanel closeBtn = new JPanel() {
            private boolean hovered = false;
            {
                setPreferredSize(new Dimension(50, 58));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        confirmed = false;
                        dispose();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() - 28;
                int cy = getHeight() / 2;
                int r = 16;

                g2.setColor(hovered ? new Color(245, 65, 65) : new Color(225, 45, 45));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                int d = 6;
                g2.drawLine(cx - d, cy - d, cx + d, cy + d);
                g2.drawLine(cx + d, cy - d, cx - d, cy + d);

                g2.dispose();
            }
        };
        headerPanel.add(closeBtn, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2. PHẦN TRUNG TÂM (4 thẻ công trình tương ứng 4 cấp)
        JPanel centerPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));

        String[] cardTitles = {"Đất nền", "Nhà phố", "Chung cư", "Khách sạn"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            cardPanels[i] = new CardPanel(idx, cardTitles[idx]);
            centerPanel.add(cardPanels[i]);
        }
        root.add(centerPanel, BorderLayout.CENTER);

        // 3. PHẦN ĐÁY (Bảng chi phí & Nút MUA Vàng Kim)
        JPanel bottomPanel = new JPanel(new BorderLayout(14, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 16, 18));
        bottomPanel.setPreferredSize(new Dimension(getWidth(), 120));

        JPanel infoBlock = new JPanel(new GridLayout(3, 1, 0, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(236, 230, 218));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        infoBlock.setOpaque(false);
        infoBlock.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        totalCostLabel = new JLabel("", SwingConstants.RIGHT);
        totalCostLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        totalCostLabel.setForeground(new Color(185, 75, 10));

        rentLabel = new JLabel("", SwingConstants.RIGHT);
        rentLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        rentLabel.setForeground(new Color(30, 130, 60));

        infoBlock.add(createInfoRow("Phí xây dựng", totalCostLabel));
        infoBlock.add(createInfoRow("Giảm phí", createStaticLabel("0")));
        infoBlock.add(createInfoRow("Phí tham quan", rentLabel));

        bottomPanel.add(infoBlock, BorderLayout.CENTER);

        // Nút MUA vàng kim
        buyButton = createGoldenBuyButton();
        buyButton.setPreferredSize(new Dimension(215, 100));
        bottomPanel.add(buyButton, BorderLayout.EAST);

        root.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(root);

        updateCalculations();
    }

    private JPanel createInfoRow(String title, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitle.setForeground(new Color(0, 110, 115));
        lblTitle.setPreferredSize(new Dimension(115, 22));

        JLabel arrows = new JLabel(">>", SwingConstants.CENTER);
        arrows.setFont(new Font("SansSerif", Font.BOLD, 14));
        arrows.setForeground(new Color(0, 155, 225));
        arrows.setPreferredSize(new Dimension(30, 22));

        valueLabel.setPreferredSize(new Dimension(95, 22));

        row.add(lblTitle, BorderLayout.WEST);
        row.add(arrows, BorderLayout.CENTER);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel createStaticLabel(String val) {
        JLabel l = new JLabel(val, SwingConstants.RIGHT);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(new Color(70, 80, 95));
        l.setPreferredSize(new Dimension(95, 22));
        return l;
    }

    private JPanel createGoldenBuyButton() {
        JPanel btn = new JPanel(new BorderLayout(0, 0)) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onBuyClicked();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                boolean canAfford = isNextLevelSelected && player.getBalance() >= finalTotalCost && finalTotalCost > 0;

                if (canAfford) {
                    Color top = hovered ? new Color(255, 240, 115) : new Color(255, 225, 70);
                    Color bot = hovered ? new Color(255, 175, 0) : new Color(245, 150, 0);
                    GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bot);
                    g2.setPaint(gp);
                } else {
                    g2.setColor(new Color(185, 185, 185));
                }
                g2.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);

                g2.setColor(canAfford ? new Color(225, 115, 0) : new Color(140, 140, 140));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, 18, 18);

                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(4, 4, w - 8, h / 2 - 2, 14, 14);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        String actionText = currentLevel == 0 ? "MUA" : "NÂNG CẤP";
        lblMuaText = new JLabel(actionText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(getText());
                int x = (getWidth() - textW) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() + 2;

                g2.setColor(new Color(140, 70, 0, 190));
                g2.drawString(getText(), x, y + 2);

                g2.setColor(new Color(255, 255, 240));
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        lblMuaText.setFont(new Font("SansSerif", Font.BOLD, currentLevel == 0 ? 30 : 22));

        JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fillRoundRect(8, 0, getWidth() - 16, getHeight() - 2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pillPanel.setOpaque(false);
        pillPanel.setPreferredSize(new Dimension(180, 32));

        btnCostLabel = new JLabel();
        btnCostLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCostLabel.setForeground(Color.WHITE);

        JLabel moneyIcon = new JLabel("💵");
        moneyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));

        pillPanel.add(moneyIcon);
        pillPanel.add(btnCostLabel);

        btn.add(lblMuaText, BorderLayout.CENTER);
        btn.add(pillPanel, BorderLayout.SOUTH);
        return btn;
    }

    private void onBuyClicked() {
        if (!isNextLevelSelected) {
            confirmed = false;
            dispose();
            return;
        }

        if (player.getBalance() < finalTotalCost) {
            JOptionPane.showMessageDialog(this,
                "Số dư không đủ! Bạn cần $" + finalTotalCost + " nhưng chỉ có $" + player.getBalance() + ".",
                "Không Đủ Tiền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    private void updateCalculations() {
        if (isNextLevelSelected && nextLevelIndex < 4) {
            finalTotalCost = costs[nextLevelIndex];
            finalTargetLevel = nextLevelIndex + 1; // Cấp 1..4
            totalCostLabel.setText(formatMoney(finalTotalCost));
            rentLabel.setText(formatMoney(rents[nextLevelIndex]));
            btnCostLabel.setText(formatMoney(finalTotalCost));
        } else {
            finalTotalCost = 0;
            finalTargetLevel = currentLevel;
            totalCostLabel.setText("$0");
            int curRent = currentLevel > 0 ? rents[currentLevel - 1] : 0;
            rentLabel.setText(formatMoney(curRent));
            btnCostLabel.setText("$0");
        }

        for (CardPanel cp : cardPanels) {
            cp.repaint();
        }
        buyButton.repaint();
    }

    private String formatMoney(int amount) {
        if (amount >= 1000) {
            int k = amount / 1000;
            int r = amount % 1000;
            if (r == 0) return k + "K";
            return k + "K" + (r < 100 ? "0" + r : r);
        }
        return "$" + amount;
    }

    // =========================================================
    // LỚP VẼ TỪNG THẺ CÔNG TRÌNH (CHỈ CHO MUA THÊM 1 CẤP)
    // =========================================================
    private class CardPanel extends JPanel {
        private final int index; // 0: Đất nền, 1: Nhà phố, 2: Chung cư, 3: Khách sạn
        private final String title;
        private boolean hovered = false;

        CardPanel(int index, String title) {
            this.index = index;
            this.title = title;
            setOpaque(false);

            boolean isCurrentTarget = (index == nextLevelIndex);
            if (isCurrentTarget) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        isNextLevelSelected = !isNextLevelSelected;
                        updateCalculations();
                    }
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            boolean isAlreadyOwned = (index < currentLevel);
            boolean isCurrentTarget = (index == nextLevelIndex);
            boolean isLocked = (index > nextLevelIndex);

            // 1. Nền thẻ
            if (isLocked) {
                g2.setColor(new Color(245, 247, 250));
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillRoundRect(0, 0, w, h, 14, 14);

            // 2. Viền chọn
            if (isCurrentTarget && isNextLevelSelected) {
                g2.setColor(new Color(0, 160, 235));
                g2.setStroke(new BasicStroke(2.5f));
            } else if (isAlreadyOwned) {
                g2.setColor(new Color(75, 185, 110));
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(hovered && isCurrentTarget ? new Color(180, 205, 225) : new Color(220, 228, 235));
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.drawRoundRect(1, 1, w - 2, h - 2, 14, 14);

            // 3. Thanh tiêu đề xanh biển trên mỗi thẻ
            if (isLocked) {
                g2.setColor(new Color(150, 165, 180));
            } else if (isAlreadyOwned) {
                g2.setColor(new Color(45, 165, 85));
            } else {
                g2.setColor(new Color(2, 140, 215));
            }
            g2.fillRoundRect(2, 2, w - 4, 32, 12, 12);
            g2.fillRect(2, 18, w - 4, 14);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(title)) / 2;
            g2.drawString(title, tx, 22);

            // 4. Vẽ nền gạch đất Isometric 3D
            drawIsometricPlatform(g2, w / 2, h / 2 + 16, isLocked);

            // 5. Vẽ mô hình công trình
            switch (index) {
                case 0: drawFlagPlot(g2, w / 2, h / 2 + 10, isLocked); break;
                case 1: drawTownhouse(g2, w / 2, h / 2 + 10, isLocked); break;
                case 2: drawApartment(g2, w / 2, h / 2 + 10, isLocked); break;
                case 3: drawSkyscraper(g2, w / 2, h / 2 + 10, isLocked); break;
            }

            // 6. Thanh trạng thái & giá tiền ở chân thẻ
            drawBottomStatus(g2, w, h, isAlreadyOwned, isCurrentTarget, isLocked);

            g2.dispose();
            super.paintComponent(g);
        }

        private void drawIsometricPlatform(Graphics2D g2, int cx, int cy, boolean isLocked) {
            int pw = 50, ph = 26;

            Polygon base = new Polygon();
            base.addPoint(cx - pw, cy);
            base.addPoint(cx, cy + ph);
            base.addPoint(cx, cy + ph + 8);
            base.addPoint(cx - pw, cy + 8);
            g2.setColor(isLocked ? new Color(215, 220, 225) : new Color(205, 215, 225));
            g2.fillPolygon(base);

            Polygon baseRight = new Polygon();
            baseRight.addPoint(cx, cy + ph);
            baseRight.addPoint(cx + pw, cy);
            baseRight.addPoint(cx + pw, cy + 8);
            baseRight.addPoint(cx, cy + ph + 8);
            g2.setColor(isLocked ? new Color(200, 205, 210) : new Color(185, 198, 212));
            g2.fillPolygon(baseRight);

            Polygon top = new Polygon();
            top.addPoint(cx, cy - ph);
            top.addPoint(cx + pw, cy);
            top.addPoint(cx, cy + ph);
            top.addPoint(cx - pw, cy);
            g2.setColor(isLocked ? new Color(240, 242, 245) : new Color(248, 252, 255));
            g2.fillPolygon(top);

            g2.setColor(new Color(215, 225, 235));
            g2.setStroke(new BasicStroke(1f));
            g2.drawPolygon(top);
        }

        private void drawFlagPlot(Graphics2D g2, int cx, int cy, boolean isLocked) {
            int bx = cx - 2;
            int by = cy + 2;

            g2.setColor(isLocked ? new Color(160, 175, 190) : new Color(60, 130, 240));
            g2.fillOval(bx - 12, by - 5, 24, 10);
            g2.setColor(isLocked ? new Color(130, 145, 160) : new Color(30, 90, 190));
            g2.drawOval(bx - 12, by - 5, 24, 10);

            g2.setColor(new Color(210, 220, 230));
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawLine(bx, by - 28, bx, by);

            g2.setColor(isLocked ? new Color(190, 190, 190) : new Color(255, 215, 0));
            g2.fillOval(bx - 2, by - 31, 5, 5);

            Polygon flag = new Polygon();
            flag.addPoint(bx + 1, by - 28);
            flag.addPoint(bx + 20, by - 20);
            flag.addPoint(bx + 1, by - 12);
            g2.setColor(isLocked ? new Color(165, 180, 195) : new Color(40, 130, 250));
            g2.fillPolygon(flag);
            g2.setColor(isLocked ? new Color(135, 150, 165) : new Color(20, 80, 190));
            g2.drawPolygon(flag);
        }

        private void drawTownhouse(Graphics2D g2, int cx, int cy, boolean isLocked) {
            int hx = cx - 16;
            int hy = cy - 20;

            g2.setColor(isLocked ? new Color(230, 235, 240) : new Color(240, 245, 250));
            g2.fillRect(hx, hy + 12, 32, 16);
            g2.setColor(new Color(180, 195, 210));
            g2.drawRect(hx, hy + 12, 32, 16);

            g2.setColor(isLocked ? new Color(160, 175, 190) : new Color(60, 140, 240));
            g2.fillRect(hx + 12, hy + 16, 8, 12);

            Polygon roof = new Polygon();
            roof.addPoint(cx, hy);
            roof.addPoint(hx + 36, hy + 12);
            roof.addPoint(hx - 4, hy + 12);
            g2.setColor(isLocked ? new Color(145, 165, 185) : new Color(30, 120, 230));
            g2.fillPolygon(roof);
            g2.setColor(isLocked ? new Color(125, 140, 160) : new Color(15, 75, 175));
            g2.drawPolygon(roof);
        }

        private void drawApartment(Graphics2D g2, int cx, int cy, boolean isLocked) {
            int ax = cx - 18;
            int ay = cy - 32;

            g2.setColor(isLocked ? new Color(225, 230, 235) : new Color(235, 242, 250));
            g2.fillRect(ax, ay + 12, 36, 28);
            g2.setColor(new Color(170, 190, 210));
            g2.drawRect(ax, ay + 12, 36, 28);

            g2.setColor(isLocked ? new Color(175, 190, 205) : new Color(100, 180, 255));
            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 3; c++) {
                    g2.fillRect(ax + 5 + c * 10, ay + 16 + r * 10, 6, 6);
                }
            }

            g2.setColor(isLocked ? new Color(140, 160, 180) : new Color(25, 105, 215));
            g2.fillRoundRect(ax - 2, ay + 6, 40, 8, 4, 4);
        }

        private void drawSkyscraper(Graphics2D g2, int cx, int cy, boolean isLocked) {
            int sx = cx - 16;
            int sy = cy - 42;

            g2.setColor(isLocked ? new Color(150, 165, 185) : new Color(40, 130, 235));
            g2.fillRoundRect(sx, sy + 6, 32, 44, 4, 4);
            g2.setColor(isLocked ? new Color(120, 135, 150) : new Color(20, 80, 180));
            g2.drawRoundRect(sx, sy + 6, 32, 44, 4, 4);

            g2.setColor(isLocked ? new Color(200, 210, 220) : new Color(160, 220, 255, 180));
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 3; c++) {
                    g2.fillRect(sx + 4 + c * 9, sy + 10 + r * 9, 6, 5);
                }
            }

            g2.setColor(new Color(210, 220, 240));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(cx, sy + 6, cx, sy - 2);
            g2.setColor(isLocked ? new Color(180, 180, 180) : new Color(255, 215, 0));
            g2.fillOval(cx - 2, sy - 4, 4, 4);
        }

        private void drawBottomStatus(Graphics2D g2, int w, int h, boolean isAlreadyOwned, boolean isCurrentTarget, boolean isLocked) {
            int by = h - 28;
            int cx = 20;
            int cy = by + 10;
            int r = 10;

            if (isAlreadyOwned) {
                // Đã có
                g2.setColor(new Color(45, 175, 85));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                g2.drawLine(cx - 1, cy + 4, cx + 5, cy - 4);

                g2.setColor(new Color(45, 150, 75));
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString("ĐÃ CÓ", cx + r + 6, cy + 5);
            } else if (isCurrentTarget) {
                // Cấp duy nhất được mua/nâng cấp
                if (isNextLevelSelected) {
                    g2.setColor(new Color(85, 195, 60));
                    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                    g2.setColor(new Color(55, 155, 35));
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                    g2.drawLine(cx - 1, cy + 4, cx + 5, cy - 4);
                } else {
                    g2.setColor(new Color(220, 225, 230));
                    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                    g2.setColor(new Color(175, 185, 195));
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                }

                String text = formatMoney(costs[index]);
                g2.setColor(new Color(30, 40, 55));
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString(text, cx + r + 6, cy + 5);
            } else {
                // Bị khóa (cấp sau)
                g2.setColor(new Color(215, 220, 225));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                g2.setColor(new Color(150, 160, 170));
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.drawString("CẤP SAU", cx + r + 4, cy + 4);
            }
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getFinalTargetLevel() {
        return finalTargetLevel;
    }

    public int getFinalTotalCost() {
        return finalTotalCost;
    }
}
