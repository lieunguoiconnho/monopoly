package ProjectOop.ui;

import ProjectOop.board.Board;
import ProjectOop.player.Player;
import ProjectOop.square.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * GameUI v2 – Giao diện đồ hoạ Swing với icon từng ô, nhà/khách sạn,
 * và nhân vật hoạt hình cartoon đơn giản.
 */
public class GameUI extends JFrame implements GameEngine.GameListener {

    // ══════════ PALETTE ══════════════════════════════════════════
    private static final Color BG_DARK   = new Color(12, 16, 30);
    private static final Color BG_PANEL  = new Color(20, 27, 48);
    private static final Color BG_CARD   = new Color(28, 38, 62);
    private static final Color C_GOLD    = new Color(255, 210, 0);
    private static final Color C_GREEN   = new Color(60, 200, 120);
    private static final Color C_RED     = new Color(235, 75, 75);
    private static final Color C_BLUE    = new Color(80, 140, 255);
    private static final Color C_TEAL    = new Color(0, 195, 175);
    private static final Color TEXT_HI   = new Color(240, 245, 255);
    private static final Color TEXT_DIM  = new Color(130, 145, 180);
    private static final Color BORDER    = new Color(50, 68, 110);

    // ── Nhóm màu đất ──────────────────────────────────────────────
    private static final Color[] GROUP_C = {
        new Color(145, 55, 200),  // PURPLE
        new Color(80, 210, 230),  // LIGHT_BLUE
        new Color(230, 80, 150),  // PINK
        new Color(255, 140, 30),  // ORANGE
        new Color(220, 45, 45),   // RED
        new Color(230, 210, 40),  // YELLOW
        new Color(50, 185, 70),   // GREEN
        new Color(25, 90, 220),   // DARK_BLUE
    };

    // ── Token & Màu Người Chơi (Xanh dương, Xanh lá, Đỏ, Vàng theo số lượng người chơi) ──
    private static final Color[] P_COL = {
        new Color(40,  135, 255), // P1: Xanh dương (Blue)
        new Color(40,  205, 110), // P2: Xanh lá (Green)
        new Color(245, 65,  65),  // P3: Đỏ (Red)
        new Color(255, 205, 30),  // P4: Vàng (Yellow)
    };
    private static final String[] P_COL_NAMES = {
        "Xanh dương",
        "Xanh lá",
        "Đỏ",
        "Vàng"
    };
    // Màu tóc/nón cartoon tương ứng
    private static final Color[] P_HAIR = {
        new Color(20,  70,  160), // P1: Xanh sẫm
        new Color(20,  125, 55),  // P2: Xanh lá sẫm
        new Color(150, 25,  25),  // P3: Đỏ sẫm
        new Color(170, 115, 10),  // P4: Nâu vàng
    };
    private static final String[] P_INITIALS = {"P1", "P2", "P3", "P4"};

    /** Helper tính màu chữ tương phản (trắng hoặc đen) để luôn dễ nhìn */
    private static Color getContrastColor(Color c) {
        if (c == null) return Color.WHITE;
        double lum = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
        return lum > 165 ? new Color(20, 25, 35) : Color.WHITE;
    }

    // ── Asset Images từ asset.png ─────────────────────────────────
    private static BufferedImage IMG_BAT_DAU;
    private static BufferedImage IMG_BAI_DO_XE;
    private static BufferedImage IMG_VAO_TU;
    private static BufferedImage IMG_CO_HOI;
    private static BufferedImage IMG_THUE;
    private static BufferedImage IMG_NHA;

    static {
        IMG_BAT_DAU   = loadAsset("bat_dau.png");
        IMG_BAI_DO_XE = loadAsset("bai_do_xe.png");
        IMG_VAO_TU    = loadAsset("vao_tu.png");
        IMG_CO_HOI    = loadAsset("co_hoi.png");
        IMG_THUE      = loadAsset("thue.png");
        IMG_NHA       = loadAsset("nha.png");
    }

    private static BufferedImage loadAsset(String filename) {
        String[] possiblePaths = {
            "assets/" + filename,
            "assets\\" + filename,
            "../assets/" + filename,
            "ProjectOop/assets/" + filename,
            "ProjectOop/ProjectOop/assets/" + filename
        };
        for (String p : possiblePaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try {
                    return ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        try {
            java.net.URL url = GameUI.class.getResource("/assets/" + filename);
            if (url == null) url = GameUI.class.getClassLoader().getResource("assets/" + filename);
            if (url == null) url = GameUI.class.getResource(filename);
            if (url != null) return ImageIO.read(url);
        } catch (Exception ignored) {}
        System.err.println("Warning: Could not load asset: " + filename);
        return null;
    }

    private static void drawScaledImage(Graphics2D g2, BufferedImage img, int cx, int cy, int maxW, int maxH) {
        if (img == null) return;
        int iw = img.getWidth();
        int ih = img.getHeight();
        double scale = Math.min((double) maxW / iw, (double) maxH / ih);
        int dw = (int) Math.round(iw * scale);
        int dh = (int) Math.round(ih * scale);
        int x = cx - dw / 2;
        int y = cy - dh / 2;

        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, x, y, dw, dh, null);
        if (oldHint != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
        }
    }


    // ══════════ FIELDS ════════════════════════════════════════════
    private GameEngine engine;
    private BoardPanel boardPanel;
    private JPanel sidePanel, actionPanel;
    private JTextArea logArea;
    private JButton rollBtn;
    private JButton buyYesBtn, buyNoBtn;
    private JLabel buyPromptLabel;
    private JButton jailPayBtn, jailCardBtn, jailRollBtn;
    private DiceFace dice1Face, dice2Face;
    private JLabel currentPlayerLabel;
    private JPanel[]  playerCards;
    private JLabel[]  playerMoneyLabels, playerPosLabels;
    private JComboBox<String> upgradeCombo;

    private Timer diceAnimTimer;
    private int   diceAnimCount;

    // ══════════ CONSTRUCTOR ═══════════════════════════════════════
    public GameUI() {
        setTitle("🎲 Cờ Tỷ Phú Việt Nam");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        showSetupScreen();
        setVisible(true);
    }

    // ══════════ SETUP SCREEN ══════════════════════════════════════
    private void showSetupScreen() {
        JPanel root = new GradPanel(BG_DARK, new Color(14, 28, 55));
        root.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy=0; g.insets=new Insets(44,40,4,40);
        JLabel t1 = lbl("🎲 CỜ TỶ PHÚ VIỆT NAM", 34, Font.BOLD, C_GOLD);
        t1.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(t1, g);

        g.gridy=1; g.insets=new Insets(0,40,28,40);
        JLabel t2 = lbl("MONOPOLY – PHIÊN BẢN ĐỊA DANH VIỆT NAM", 13, Font.PLAIN, TEXT_DIM);
        t2.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(t2, g);

        g.gridy=2; g.insets=new Insets(6,80,4,80);
        JLabel nl = lbl("Số người chơi (2 – 4):", 15, Font.BOLD, TEXT_HI);
        nl.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(nl, g);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2,2,4,1));
        spinner.setFont(new Font("SansSerif",Font.BOLD,18));
        styleSpinner(spinner);
        g.gridy=3; g.insets=new Insets(0,160,20,160);
        root.add(spinner, g);

        JTextField[] fields = new JTextField[4];
        String[] defs = {"Người Chơi 1","Người Chơi 2","Người Chơi 3","Người Chơi 4"};
        JPanel namePanel = new JPanel(new GridLayout(4,1,4,8));
        namePanel.setOpaque(false);
        JLabel[] colLabels = new JLabel[4];
        for (int i=0;i<4;i++){
            JPanel row=new JPanel(new BorderLayout(10,0));
            row.setOpaque(false);
            JLabel ico = new JLabel("● "+P_INITIALS[i]+" ("+P_COL_NAMES[i]+"):");
            ico.setFont(new Font("SansSerif",Font.BOLD,13));
            ico.setForeground(P_COL[i]);
            ico.setPreferredSize(new Dimension(145,28));
            colLabels[i] = ico;
            fields[i]=new JTextField(defs[i]);
            styleTextField(fields[i]);
            row.add(ico,BorderLayout.WEST);
            row.add(fields[i],BorderLayout.CENTER);
            namePanel.add(row);
        }
        fields[2].setEnabled(false); fields[2].setForeground(TEXT_DIM); colLabels[2].setForeground(TEXT_DIM);
        fields[3].setEnabled(false); fields[3].setForeground(TEXT_DIM); colLabels[3].setForeground(TEXT_DIM);
        spinner.addChangeListener(e->{
            int n=(int)spinner.getValue();
            for(int i=0;i<4;i++){
                fields[i].setEnabled(i<n);
                fields[i].setForeground(i<n?TEXT_HI:TEXT_DIM);
                colLabels[i].setForeground(i<n?P_COL[i]:TEXT_DIM);
            }
        });
        g.gridy=4; g.insets=new Insets(0,40,18,40);
        root.add(namePanel,g);

        JButton start = btn("▶  BẮT ĐẦU GAME", C_GOLD, BG_DARK);
        start.setFont(new Font("SansSerif",Font.BOLD,18));
        start.setPreferredSize(new Dimension(260,52));
        start.addActionListener(e->{
            int n=(int)spinner.getValue();
            String[] names=new String[n];
            for(int i=0;i<n;i++){String s=fields[i].getText().trim();names[i]=s.isEmpty()?defs[i]:s;}
            startGame(names);
        });
        g.gridy=5; g.insets=new Insets(10,100,44,100);
        root.add(start,g);

        setContentPane(root); revalidate();
    }

    private void startGame(String[] names){
        engine=new GameEngine(names);
        engine.addListener(this);
        buildGameUI(names.length);
        JPanel root=new GradPanel(BG_DARK,new Color(8,14,26));
        root.setLayout(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.add(boardPanel,BorderLayout.CENTER);
        root.add(sidePanel,BorderLayout.EAST);
        setContentPane(root); revalidate(); repaint();
        engine.startGame();
    }

    // ══════════ BUILD GAME UI ═════════════════════════════════════
    private void buildGameUI(int n){
        boardPanel = new BoardPanel();

        sidePanel = new JPanel(new BorderLayout(0,6));
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(308,0));

        // Current player banner
        currentPlayerLabel = new JLabel("", SwingConstants.CENTER);
        currentPlayerLabel.setFont(new Font("SansSerif",Font.BOLD,13));
        currentPlayerLabel.setForeground(C_GOLD);
        currentPlayerLabel.setOpaque(true);
        currentPlayerLabel.setBackground(BG_CARD);
        currentPlayerLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER,1),
            BorderFactory.createEmptyBorder(6,8,6,8)));
        sidePanel.add(currentPlayerLabel,BorderLayout.NORTH);

        // Player cards
        JPanel pcards = new JPanel(new GridLayout(n,1,0,5));
        pcards.setOpaque(false);
        playerCards=new JPanel[n]; playerMoneyLabels=new JLabel[n]; playerPosLabels=new JLabel[n];
        for(int i=0;i<n;i++){playerCards[i]=buildPlayerCard(i,engine.getPlayers()[i]);pcards.add(playerCards[i]);}

        // Dice
        JPanel diceSection = new JPanel(new BorderLayout(0,4));
        diceSection.setOpaque(false);
        diceSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER),"  🎲  XÚC XẮC",
            TitledBorder.CENTER,TitledBorder.TOP,
            new Font("SansSerif",Font.BOLD,11),TEXT_DIM));
        JPanel diceRow=new JPanel(new FlowLayout(FlowLayout.CENTER,14,4));
        diceRow.setOpaque(false);
        dice1Face=new DiceFace(); dice2Face=new DiceFace();
        diceRow.add(dice1Face); diceRow.add(dice2Face);
        diceSection.add(diceRow,BorderLayout.CENTER);

        // Log
        logArea=new JTextArea();
        logArea.setEditable(false); logArea.setLineWrap(true); logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced",Font.PLAIN,11));
        logArea.setBackground(BG_CARD); logArea.setForeground(TEXT_HI);
        logArea.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        JScrollPane logScroll=new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER),"NHẬT KÝ TRẬN ĐẤU",
            TitledBorder.LEFT,TitledBorder.TOP,
            new Font("SansSerif",Font.BOLD,11),TEXT_DIM));
        logScroll.getViewport().setBackground(BG_CARD);

        JPanel midCenter=new JPanel(new BorderLayout(0,6));
        midCenter.setOpaque(false);
        midCenter.add(diceSection,BorderLayout.NORTH);
        midCenter.add(logScroll,BorderLayout.CENTER);

        JPanel sideCenter=new JPanel(new BorderLayout(0,6));
        sideCenter.setOpaque(false);
        sideCenter.add(pcards,BorderLayout.NORTH);
        sideCenter.add(midCenter,BorderLayout.CENTER);

        // Actions
        actionPanel=new JPanel(new CardLayout());
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(308,128));
        buildActions();

        sidePanel.add(sideCenter,BorderLayout.CENTER);
        sidePanel.add(actionPanel,BorderLayout.SOUTH);
    }

    private JPanel buildPlayerCard(int idx, Player p){
        JPanel card=new JPanel(new BorderLayout(8,3));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(P_COL[idx],1),
            BorderFactory.createEmptyBorder(7,10,7,10)));
        // Avatar panel
        PlayerAvatar avatar = new PlayerAvatar(idx, 34);
        card.add(avatar, BorderLayout.WEST);

        JPanel info=new JPanel(new GridLayout(3,1,0,1));
        info.setOpaque(false);
        JLabel nm=lbl("● "+p.getName()+" ("+P_COL_NAMES[idx]+")",12,Font.BOLD,P_COL[idx]);
        playerMoneyLabels[idx]=lbl("$"+p.getBalance(),12,Font.BOLD,C_GREEN);
        playerPosLabels[idx]=lbl("Ô 0 – GO",10,Font.PLAIN,TEXT_DIM);
        info.add(nm); info.add(playerMoneyLabels[idx]); info.add(playerPosLabels[idx]);
        card.add(info,BorderLayout.CENTER);
        return card;
    }

    private void buildActions(){
        // ── ROLL
        JPanel roll=panel(BG_CARD, C_GOLD, 10);
        roll.setLayout(new BorderLayout());
        roll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_GOLD,1),
            BorderFactory.createEmptyBorder(10,14,10,14)));
        rollBtn=btn("🎲  ĐỔ XÚC XẮC",C_GOLD,BG_DARK);
        rollBtn.setFont(new Font("SansSerif",Font.BOLD,16));
        rollBtn.addActionListener(e->{rollBtn.setEnabled(false);animDice(()->engine.actionRoll());});
        roll.add(rollBtn,BorderLayout.CENTER);
        actionPanel.add(roll,"ROLL");

        // ── JAIL
        JPanel jail=new JPanel(new BorderLayout(0,6));
        jail.setBackground(BG_CARD);
        jail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_RED,1),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        JLabel jt=lbl("🔒  Đang ở TÙ – Chọn hành động:",12,Font.BOLD,C_RED);
        jt.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel jrow=new JPanel(new GridLayout(1,3,6,0)); jrow.setOpaque(false);
        jailPayBtn=btn("<html><center>💰<br>Nộp $50</center></html>",C_GREEN,BG_DARK);
        jailCardBtn=btn("<html><center>🃏<br>Dùng thẻ</center></html>",C_BLUE,BG_DARK);
        jailRollBtn=btn("<html><center>🎲<br>Thử đôi</center></html>",C_GOLD,BG_DARK);
        jailPayBtn.addActionListener(e->{disableJail();animDice(()->engine.actionJail(1));});
        jailCardBtn.addActionListener(e->{disableJail();engine.actionJail(2);});
        jailRollBtn.addActionListener(e->{disableJail();animDice(()->engine.actionJail(3));});
        jrow.add(jailPayBtn);jrow.add(jailCardBtn);jrow.add(jailRollBtn);
        jail.add(jt,BorderLayout.NORTH); jail.add(jrow,BorderLayout.CENTER);
        actionPanel.add(jail,"JAIL");

        // ── BUY
        JPanel buy=new JPanel(new BorderLayout(0,8)); buy.setBackground(BG_CARD);
        buy.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_GREEN,1),
            BorderFactory.createEmptyBorder(10,14,10,14)));
        buyPromptLabel=lbl("💰  Bạn có muốn mua ô đất này?",12,Font.BOLD,C_GREEN);
        buyPromptLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bb=new JPanel(new GridLayout(1,2,10,0)); bb.setOpaque(false);
        buyYesBtn=btn("✅  MUA",C_GREEN,BG_DARK);
        buyNoBtn=btn("❌  BỎ QUA",C_RED,BG_DARK);
        buyYesBtn.addActionListener(e->engine.actionBuy(true));
        buyNoBtn.addActionListener(e->engine.actionBuy(false));
        bb.add(buyYesBtn); bb.add(buyNoBtn);
        buy.add(buyPromptLabel,BorderLayout.NORTH); buy.add(bb,BorderLayout.SOUTH);
        actionPanel.add(buy,"BUY");

        // ── UPGRADE
        JPanel up=new JPanel(new BorderLayout(0,8)); up.setBackground(BG_CARD);
        up.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BLUE,1),
            BorderFactory.createEmptyBorder(8,12,8,12)));
        JLabel ut=lbl("🏠  Vào ô của mình – Nâng cấp (1 lần):",12,Font.BOLD,C_BLUE);
        ut.setHorizontalAlignment(SwingConstants.CENTER);
        upgradeCombo=new JComboBox<>();
        upgradeCombo.setBackground(BG_CARD); upgradeCombo.setForeground(TEXT_HI);
        upgradeCombo.setFont(new Font("SansSerif",Font.PLAIN,11));
        JPanel ub=new JPanel(new GridLayout(1,2,8,0)); ub.setOpaque(false);
        JButton doU=btn("🏠 NÂNG CẤP",C_BLUE,BG_DARK);
        JButton skipU=btn("⏭ BỎ QUA",new Color(70,82,120),BG_DARK);
        doU.addActionListener(e->engine.actionUpgrade(0));
        skipU.addActionListener(e->engine.actionUpgrade(-1));
        ub.add(doU); ub.add(skipU);
        up.add(ut,BorderLayout.NORTH); up.add(upgradeCombo,BorderLayout.CENTER); up.add(ub,BorderLayout.SOUTH);
        actionPanel.add(up,"UPGRADE");

        // ── GAME OVER
        JPanel over=new JPanel(new BorderLayout(0,8)); over.setBackground(BG_CARD);
        over.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_GOLD,1),
            BorderFactory.createEmptyBorder(10,14,10,14)));
        JLabel ol=lbl("🏆  GAME KẾT THÚC",20,Font.BOLD,C_GOLD);
        ol.setHorizontalAlignment(SwingConstants.CENTER);
        JButton restart=btn("🔄  CHƠI LẠI",C_GOLD,BG_DARK);
        restart.addActionListener(e->{dispose();new GameUI();});
        over.add(ol,BorderLayout.CENTER); over.add(restart,BorderLayout.SOUTH);
        actionPanel.add(over,"GAMEOVER");

        showCard("ROLL");
    }

    // ══════════ LISTENER CALLBACKS ════════════════════════════════
    @Override public void onGameStarted(){ log("=== BẮT ĐẦU VÁN ĐẤU ==="); boardPanel.repaint(); }
    @Override public void onTurnStart(Player p,GameEngine.TurnPhase ph){
        SwingUtilities.invokeLater(()->{
            updateHeader(p); updateCards(); updateActions(ph);
            log("\n[Lượt: " + p.getName() + " | Vốn: $" + p.getBalance() + "]");
        });
    }
    @Override public void onDiceRolled(int d1,int d2,boolean db){
        SwingUtilities.invokeLater(()->{
            dice1Face.setValue(d1);dice2Face.setValue(d2);dice1Face.repaint();dice2Face.repaint();
            log("- Đổ xúc xắc: " + d1 + " + " + d2 + " = " + (d1+d2) + (db ? " (Đổ đôi!)" : ""));
        });
    }
    @Override public void onPlayerMoved(Player p,int from,int to){
        SwingUtilities.invokeLater(()->{
            String sqName = (engine != null && engine.getBoard() != null)
                ? engine.getBoard().getSquare(to).getName()
                : ("Ô " + to);
            log("- Đến: " + sqName + " (ô " + to + ")");
            boardPanel.animMove(p,from,to);
            updateCards();
        });
    }

    @Override public void onPassGo(Player p, int bonus){
        SwingUtilities.invokeLater(()->{
            log("- Qua ô Bắt Đầu (GO): Nhận +$" + bonus);
            updateCards();
            boardPanel.repaint();
            showPassGoDialog(p, bonus);
        });
    }

    @Override public void onSquareNotice(Player p, String title, String message, String noticeType){
        SwingUtilities.invokeLater(()->{
            if ("TAX".equals(noticeType)) {
                if (engine != null && engine.getBoard() != null) {
                    Square sq = engine.getBoard().getSquare(p.getPosition());
                    if (sq instanceof TaxSquare) {
                        TaxSquare tax = (TaxSquare) sq;
                        log("- Nộp thuế: -$" + tax.getTaxAmount() + " (" + tax.getName() + ")");
                    }
                }
            } else if ("JAIL".equals(noticeType)) {
                log("- Bị bắt vào tù (chuyển đến ô 10)!");
            } else if ("INFO".equals(noticeType)) {
                if (p.getPosition() == 20) {
                    log("- Nghỉ chân tại Bãi Đỗ Xe Miễn Phí");
                } else if (p.getPosition() == 10) {
                    log("- Thăm tù (Nghỉ chân an toàn)");
                }
            } else if ("WARNING".equals(noticeType)) {
                log("- Không đủ tiền mua đất");
            }
            updateCards();
            boardPanel.repaint();
            showSquareNoticeDialog(p, title, message, noticeType);
        });
    }

    @Override public void onBuyPrompt(Player p,PropertySquare prop){
        SwingUtilities.invokeLater(()->{
            buyPromptLabel.setText("Mua " + trunc(prop.getName(),14) + " ($" + prop.getPrice() + ")?");
            showCard("BUY");

            int res = JOptionPane.showConfirmDialog(
                GameUI.this,
                "<html><div style='font-family:sans-serif;padding:6px;width:250px;'>"
                + "<h3 style='color:#00C3AF;margin:0 0 6px 0;'>THÔNG BÁO MUA ĐẤT</h3>"
                + "<p>Người chơi: <b>" + p.getName() + "</b></p>"
                + "<p>Dừng tại: <b>" + prop.getName() + "</b> (Ô " + prop.getPosition() + ")</p>"
                + "<p>Giá bán: <b style='color:#3CD078;'>$" + prop.getPrice() + "</b> | Số dư: <b>$" + p.getBalance() + "</b></p>"
                + "<p>Bạn có muốn mua bất động sản này không?</p></div></html>",
                "Mua Đất - " + prop.getName(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (res == JOptionPane.YES_OPTION) {
                engine.actionBuy(true);
            } else {
                engine.actionBuy(false);
            }
        });
    }

    @Override public void onRentPaid(Player p, Player owner, PropertySquare prop, int rent){
        SwingUtilities.invokeLater(()->{
            log("- Trả $" + rent + " tiền thuê cho " + owner.getName() + " tại " + prop.getName());
            updateCards();
            boardPanel.repaint();

            String msg = "Bạn dừng tại <b>" + prop.getName() + "</b> thuộc sở hữu của <b>" + owner.getName() + "</b>.<br>"
                + "Tiền thuê nhà: <b style='color:#FF5050;'>-$" + rent + "</b><br>"
                + "Số dư còn lại: <b>$" + p.getBalance() + "</b>";
            showSquareNoticeDialog(p, "TRẢ TIỀN THUÊ NHÀ", msg, "RENT");
        });
    }

    @Override public void onCardDrawn(Player p, String cardType, ProjectOop.card.Card card){
        SwingUtilities.invokeLater(()->{
            log("- Rút thẻ [" + cardType + "]: " + card.getDescription());
            updateCards();
            boardPanel.repaint();
            showCardDialog(p, cardType, card);
        });
    }

    private void showPassGoDialog(Player p, int bonus) {
        boolean landedOnGo = (p.getPosition() == 0);
        String dialogTitle = landedOnGo ? "Dừng Tại Ô Bắt Đầu (GO)" : "Vượt Qua Ô Bắt Đầu (GO)";
        JDialog d = new JDialog(this, dialogTitle, true);
        d.setSize(420, 250);
        d.setLocationRelativeTo(this);
        Color topC = C_GOLD;

        JPanel pan = new GradPanel(BG_DARK, BG_PANEL);
        pan.setLayout(new BorderLayout(0, 10));
        pan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(topC, 2),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = lbl(landedOnGo ? "🚩 DỪNG TẠI Ô BẮT ĐẦU (GO)" : "🎉 QUA Ô BẮT ĐẦU (GO)", 17, Font.BOLD, topC);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        String contextMsg = landedOnGo
            ? "Bạn đã hoàn thành 1 vòng đi và dừng chân chính xác tại <b>Ô Bắt Đầu (GO)</b>!"
            : "Bạn đã hoàn thành 1 vòng đi và đi qua <b>Ô Bắt Đầu (GO)</b>!";

        JLabel desc = new JLabel("<html><div style='text-align:center;font-family:sans-serif;color:#F0F5FF;padding:6px;'>"
            + "<p style='color:#8291B4;'>Người chơi: <b>" + p.getName() + "</b></p>"
            + "<p style='font-size:15px;color:#3CD078;font-weight:bold;margin:8px 0;'>+ $" + bonus + " TIỀN THƯỞNG HOÀN THÀNH VÒNG!</p>"
            + "<p style='font-size:12px;color:#CAD5EC;line-height:1.4;'>" + contextMsg + "</p>"
            + "<p style='font-size:13px;color:#FFD200;margin-top:6px;'>Số dư hiện tại: <b>$" + p.getBalance() + "</b></p>"
            + "</div></html>", SwingConstants.CENTER);

        JButton ok = btn("💰  NHẬN TIỀN", topC, BG_DARK);
        ok.setFont(new Font("SansSerif", Font.BOLD, 13));
        ok.setPreferredSize(new Dimension(140, 36));
        ok.addActionListener(e -> d.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setOpaque(false);
        bp.add(ok);

        pan.add(title, BorderLayout.NORTH);
        pan.add(desc, BorderLayout.CENTER);
        pan.add(bp, BorderLayout.SOUTH);

        d.setContentPane(pan);
        Timer t = new Timer(3000, ev -> d.dispose());
        t.setRepeats(false);
        t.start();
        d.setVisible(true);
    }

    private void showSquareNoticeDialog(Player p, String titleText, String msgHtml, String noticeType) {
        JDialog d = new JDialog(this, "Thông Báo Hiệu Ứng", true);
        d.setSize(420, 260);
        d.setLocationRelativeTo(this);

        Color topC;
        if ("TAX".equals(noticeType)) topC = C_RED;
        else if ("JAIL".equals(noticeType)) topC = new Color(245, 65, 65);
        else if ("WARNING".equals(noticeType)) topC = new Color(255, 140, 30);
        else if ("RENT".equals(noticeType)) topC = new Color(235, 75, 75);
        else topC = C_TEAL;

        JPanel pan = new GradPanel(BG_DARK, BG_PANEL);
        pan.setLayout(new BorderLayout(0, 10));
        pan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(topC, 2),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = lbl(titleText, 16, Font.BOLD, topC);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel desc = new JLabel("<html><div style='text-align:center;font-family:sans-serif;color:#F0F5FF;padding:4px;line-height:1.4;'>"
            + "<p style='color:#8291B4;margin-bottom:6px;'>Người chơi: <b>" + p.getName() + "</b> (Ô " + p.getPosition() + ")</p>"
            + "<div style='font-size:13px;'>" + msgHtml + "</div>"
            + "</div></html>", SwingConstants.CENTER);

        JButton ok = btn("✔  ĐÃ HIỂU", topC, BG_DARK);
        ok.setFont(new Font("SansSerif", Font.BOLD, 13));
        ok.setPreferredSize(new Dimension(130, 36));
        ok.addActionListener(e -> d.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setOpaque(false);
        bp.add(ok);

        pan.add(title, BorderLayout.NORTH);
        pan.add(desc, BorderLayout.CENTER);
        pan.add(bp, BorderLayout.SOUTH);

        d.setContentPane(pan);
        Timer t = new Timer(3000, ev -> d.dispose());
        t.setRepeats(false);
        t.start();
        d.setVisible(true);
    }

    private void showCardDialog(Player p, String cardType, ProjectOop.card.Card card){
        JDialog d = new JDialog(this, "Rút Thẻ " + cardType, true);
        d.setSize(380, 230);
        d.setLocationRelativeTo(this);
        boolean isChance = "Cơ Hội".equals(cardType);
        Color topC = isChance ? new Color(255, 140, 30) : new Color(0, 160, 255);
        JPanel pan = new GradPanel(BG_DARK, BG_PANEL);
        pan.setLayout(new BorderLayout(0, 10));
        pan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(topC, 2),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = lbl((isChance ? "⭐ THẺ CƠ HỘI ⭐" : "🎁 THẺ KHÍ VẬN 🎁"), 16, Font.BOLD, topC);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel desc = new JLabel("<html><div style='text-align:center;font-family:sans-serif;color:#F0F5FF;padding:6px;'>"
            + "<p style='color:#8291B4;'>Người rút: <b>" + p.getName() + "</b></p>"
            + "<p style='font-size:13px;color:#FFD200;font-weight:bold;margin-top:6px;'>" + card.getDescription() + "</p>"
            + "</div></html>", SwingConstants.CENTER);

        JButton ok = btn("✔  ĐỒNG Ý", topC, BG_DARK);
        ok.setFont(new Font("SansSerif", Font.BOLD, 13));
        ok.setPreferredSize(new Dimension(140, 36));
        ok.addActionListener(e -> d.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setOpaque(false);
        bp.add(ok);

        pan.add(title, BorderLayout.NORTH);
        pan.add(desc, BorderLayout.CENTER);
        pan.add(bp, BorderLayout.SOUTH);

        d.setContentPane(pan);
        Timer t = new Timer(3500, ev -> d.dispose());
        t.setRepeats(false);
        t.start();
        d.setVisible(true);
    }
    @Override public void onJailPrompt(Player p){ SwingUtilities.invokeLater(()->{ updateHeader(p);updateCards(); log("- Ở tù (Lượt "+(p.getTurnsInJail()+1)+"/3)"); jailPayBtn.setEnabled(true);jailCardBtn.setEnabled(p.getGetOutOfJailTicket()>0);jailRollBtn.setEnabled(true); showCard("JAIL");}); }
    @Override public void onUpgradePrompt(Player p,List<PropertySquare> list){
        SwingUtilities.invokeLater(()->{
            upgradeCombo.removeAllItems();
            for(PropertySquare pr:list){
                int nextLevel=pr.getHouseLevel()+1;
                String nextStr=nextLevel==5?"Khách sạn":("Cấp "+nextLevel);
                String curStr=pr.getHouseLevel()==0?"Đất trống":("Cấp "+pr.getHouseLevel());
                upgradeCombo.addItem(pr.getName()+" ($"+pr.getHouseCost()+") ["+curStr+" → "+nextStr+"]");
            }
            if(!list.isEmpty()){
                PropertySquare pr=list.get(0);
                showCard("UPGRADE");

                int nextLevel = pr.getHouseLevel() + 1;
                String nextStr = nextLevel == 5 ? "Khách sạn" : ("Cấp " + nextLevel);
                int res = JOptionPane.showConfirmDialog(
                    GameUI.this,
                    "<html><div style='font-family:sans-serif;padding:6px;width:250px;'>"
                    + "<h3 style='color:#508CFF;margin:0 0 6px 0;'>NÂNG CẤP BẤT ĐỘNG SẢN</h3>"
                    + "<p>Người chơi: <b>" + p.getName() + "</b></p>"
                    + "<p>Dừng tại ô của mình: <b>" + pr.getName() + "</b></p>"
                    + "<p>Chi phí: <b style='color:#3CD078;'>$" + pr.getHouseCost() + "</b> để lên <b>" + nextStr + "</b></p>"
                    + "<p>Số dư hiện tại: <b>$" + p.getBalance() + "</b></p>"
                    + "<p>Bạn có muốn nâng cấp ô này ngay bây giờ không?</p></div></html>",
                    "Nâng Cấp - " + pr.getName(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (res == JOptionPane.YES_OPTION) {
                    engine.actionUpgrade(0);
                } else {
                    engine.actionUpgrade(-1);
                }
            }
        });
    }
    @Override public void onBankruptcy(Player bk,Player cr){
        SwingUtilities.invokeLater(()->{
            String m = (cr != null)
                ? (bk.getName() + " đã phá sản! Tài sản thuộc về " + cr.getName())
                : (bk.getName() + " đã phá sản! Tài sản thuộc về Ngân hàng");
            log("*** " + m + " ***");
            updateCards();
            boardPanel.repaint();
            JOptionPane pane=new JOptionPane(m,JOptionPane.WARNING_MESSAGE);
            JDialog d=pane.createDialog(this,"Phá Sản");
            Timer t=new Timer(2200,ev->d.dispose());t.setRepeats(false);t.start();d.setVisible(true);
        });
    }
    @Override public void onGameOver(Player w){
        SwingUtilities.invokeLater(()->{
            String name=w!=null?w.getName():"???";
            log("\n==============================");
            log("=== KẾT THÚC: " + name.toUpperCase() + " CHIẾN THẮNG! ===");
            log("==============================");
            showCard("GAMEOVER");
            winDialog(name);
        });
    }
    @Override public void onMessage(String m){ SwingUtilities.invokeLater(()->log("- " + m)); }
    @Override public void onBoardUpdated(){ SwingUtilities.invokeLater(()->{ boardPanel.repaint(); updateCards();}); }

    // ══════════ UI HELPERS ════════════════════════════════════════
    private void showCard(String name){ ((CardLayout)actionPanel.getLayout()).show(actionPanel,name); if("ROLL".equals(name))rollBtn.setEnabled(true); }
    private void updateActions(GameEngine.TurnPhase ph){
        switch(ph){ case WAITING_ROLL:showCard("ROLL");break;case WAITING_BUY:showCard("BUY");break;case WAITING_UPGRADE:showCard("UPGRADE");break;case WAITING_JAIL_ACTION:showCard("JAIL");break;case GAME_OVER:showCard("GAMEOVER");break; }
    }
    private void disableJail(){ jailPayBtn.setEnabled(false);jailCardBtn.setEnabled(false);jailRollBtn.setEnabled(false); }
    private void updateHeader(Player p){ int i=pidx(p);String tk=i>=0?P_INITIALS[i]:"?"; currentPlayerLabel.setText("● "+tk+"  LƯỢT: "+p.getName().toUpperCase()+"  |  $"+p.getBalance()); if(i>=0)currentPlayerLabel.setForeground(P_COL[i]); }
    private void updateCards(){
        if(engine==null)return;
        Player[]ps=engine.getPlayers();
        for(int i=0;i<ps.length;i++){
            if(playerMoneyLabels==null||i>=playerMoneyLabels.length)continue;
            Player p=ps[i];
            playerMoneyLabels[i].setText("$"+p.getBalance());
            playerMoneyLabels[i].setForeground(p.isBankrupt()?C_RED:C_GREEN);
            String sq=engine.getBoard().getSquare(p.getPosition()).getName();
            playerPosLabels[i].setText("Ô "+p.getPosition()+" – "+trunc(sq,20));
            Color brd=p==engine.getCurrentPlayer()?P_COL[i]:BORDER;
            int bw=p==engine.getCurrentPlayer()?2:1;
            playerCards[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(p.isBankrupt()?C_RED:brd,bw),
                BorderFactory.createEmptyBorder(7,10,7,10)));
            playerCards[i].setBackground(p.isBankrupt()?new Color(38,16,16):BG_CARD);
        }
    }
    private void log(String t){ logArea.append(t+"\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
    private void animDice(Runnable after){
        diceAnimCount=0; dice1Face.setAnim(true); dice2Face.setAnim(true);
        diceAnimTimer=new Timer(65,null);
        diceAnimTimer.addActionListener(e->{ dice1Face.setValue((int)(Math.random()*6)+1); dice2Face.setValue((int)(Math.random()*6)+1); dice1Face.repaint();dice2Face.repaint(); if(++diceAnimCount>=14){diceAnimTimer.stop();dice1Face.setAnim(false);dice2Face.setAnim(false);after.run();}});
        diceAnimTimer.start();
    }
    private void winDialog(String name){
        JDialog d=new JDialog(this,"KẾT QUẢ",true); d.setSize(380,200); d.setLocationRelativeTo(this);
        JPanel p=new GradPanel(BG_DARK,BG_PANEL); p.setLayout(new BorderLayout(0,14)); p.setBorder(BorderFactory.createEmptyBorder(26,28,26,28));
        JLabel l1=lbl("NGƯỜI THẮNG CUỘC",18,Font.BOLD,C_GOLD); l1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel l2=lbl(name,28,Font.BOLD,TEXT_HI); l2.setHorizontalAlignment(SwingConstants.CENTER);
        JButton ok=btn("OK",C_GOLD,BG_DARK); ok.addActionListener(e->d.dispose());
        p.add(l1,BorderLayout.NORTH);p.add(l2,BorderLayout.CENTER);p.add(ok,BorderLayout.SOUTH);
        d.setContentPane(p);d.setVisible(true);
    }
    private int pidx(Player p){ if(engine==null)return -1; Player[]ps=engine.getPlayers(); for(int i=0;i<ps.length;i++)if(ps[i]==p)return i; return -1; }
    private String trunc(String s,int m){ return s.length()>m?s.substring(0,m-1)+"…":s; }

    // ══════════ BOARD PANEL ═══════════════════════════════════════
    class BoardPanel extends JPanel {
        private int animPidx=-1, animCurPos, animTgtPos;
        private Timer moveTimer;
        BoardPanel(){ setOpaque(false); }

        void animMove(Player p,int from,int to){
            if(moveTimer!=null&&moveTimer.isRunning())moveTimer.stop();
            animPidx=pidx(p); animCurPos=from; animTgtPos=to;
            if(from==to){repaint();return;}
            moveTimer=new Timer(90,null);
            moveTimer.addActionListener(e->{ animCurPos=(animCurPos+1)%40; repaint(); if(animCurPos==animTgtPos){moveTimer.stop();animPidx=-1;}});
            moveTimer.start();
        }

        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            int sz=Math.min(getWidth(),getHeight())-18;
            int x0=(getWidth()-sz)/2, y0=(getHeight()-sz)/2;
            drawBoard(g2,x0,y0,sz);
            if(engine!=null) drawTokens(g2,x0,y0,sz);
            g2.dispose();
        }

        private int[] dims(int sz){
            // corner = 1.65 * cell,  2*corner + 9*cell = sz
            double r=1.65;
            int cell=(int)(sz/(2*r+9));
            int corner=(int)(cell*r);
            return new int[]{corner,cell};
        }

        private void drawBoard(Graphics2D g2,int x0,int y0,int sz){
            // Board shadow + bg
            g2.setColor(new Color(0,0,0,120));
            g2.fillRoundRect(x0+4,y0+4,sz,sz,18,18);
            g2.setColor(new Color(16,24,46));
            g2.fillRoundRect(x0,y0,sz,sz,18,18);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x0,y0,sz,sz,18,18);

            if(engine==null) return;
            Board bd=engine.getBoard();
            int[]d=dims(sz); int corner=d[0],cell=d[1];
            for(int i=0;i<40;i++){
                Rectangle r=cellRect(i,x0,y0,corner,cell);
                drawCell(g2,bd.getSquare(i),r,cellDir(i));
            }
            drawCenter(g2,x0+corner,y0+corner,9*cell);
        }

        private void drawCenter(Graphics2D g2,int cx,int cy,int sz){
            g2.setPaint(new GradientPaint(cx,cy,new Color(10,18,40),cx+sz,cy+sz,new Color(18,32,72)));
            g2.fillRect(cx,cy,sz,sz);
            // Decorative ring
            g2.setColor(new Color(C_GOLD.getRed(),C_GOLD.getGreen(),C_GOLD.getBlue(),55));
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{10,7},0));
            int m=sz/11; g2.drawRoundRect(cx+m,cy+m,sz-2*m,sz-2*m,24,24);
            // Stars deco
            for(int i=0;i<3;i++){
                float alpha=50+i*25;
                drawStar(g2,cx+sz/4+i*(sz/4),cy+sz/5,sz/18,5,new Color(255,215,0,(int)alpha));
            }
            // Title
            g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(18,sz/7)));
            FontMetrics fm=g2.getFontMetrics();
            g2.setColor(C_GOLD);
            String t1="CỜ TỶ PHÚ"; g2.drawString(t1,cx+(sz-fm.stringWidth(t1))/2,cy+sz/2-4);
            g2.setFont(new Font("SansSerif",Font.PLAIN,Math.max(12,sz/12)));
            fm=g2.getFontMetrics(); g2.setColor(TEXT_DIM);
            String t2="VIỆT NAM"; g2.drawString(t2,cx+(sz-fm.stringWidth(t2))/2,cy+sz/2+fm.getHeight());
        }

        // ── Cell rect ───────────────────────────────────────────
        private Rectangle cellRect(int i,int x0,int y0,int corner,int cell){
            if(i==0)  return new Rectangle(x0+corner+9*cell,y0+corner+9*cell,corner,corner);
            if(i==10) return new Rectangle(x0,              y0+corner+9*cell,corner,corner);
            if(i==20) return new Rectangle(x0,              y0,              corner,corner);
            if(i==30) return new Rectangle(x0+corner+9*cell,y0,              corner,corner);
            if(i>=1 &&i<=9)  return new Rectangle(x0+corner+(9-i)*cell,y0+corner+9*cell,cell,corner);
            if(i>=11&&i<=19) return new Rectangle(x0,y0+corner+(19-i)*cell,corner,cell);
            if(i>=21&&i<=29) return new Rectangle(x0+corner+(i-21)*cell,y0,cell,corner);
            // 31–39
            return new Rectangle(x0+corner+9*cell,y0+corner+(i-31)*cell,corner,cell);
        }
        private int cellDir(int i){ // 0=bottom,1=left,2=top,3=right,-1=corner
            if(i==0||i==10||i==20||i==30)return -1;
            if(i>=1 &&i<=9)  return 0;
            if(i>=11&&i<=19) return 1;
            if(i>=21&&i<=29) return 2;
            return 3;
        }

        // ── Draw one cell ────────────────────────────────────────
        private void drawCell(Graphics2D g2,Square sq,Rectangle r,int dir){
            // Background gradient
            Color bg1=cellBg1(sq), bg2=cellBg2(sq);
            g2.setPaint(new GradientPaint(r.x,r.y,bg1,r.x+r.width,r.y+r.height,bg2));
            g2.fillRect(r.x,r.y,r.width,r.height);

            // Color stripe for property
            if(sq instanceof PropertySquare){
                PropertySquare prop=(PropertySquare)sq;
                Color gc=groupColor(prop);
                if(gc!=null){
                    int sw=Math.max(8,Math.min(12,(dir==-1?r.width:Math.max(r.width,r.height))/7));
                    g2.setColor(gc);
                    switch(dir){
                        case 0: g2.fillRect(r.x,r.y,r.width,sw); break;
                        case 1: g2.fillRect(r.x+r.width-sw,r.y,sw,r.height); break;
                        case 2: g2.fillRect(r.x,r.y+r.height-sw,r.width,sw); break;
                        case 3: g2.fillRect(r.x,r.y,sw,r.height); break;
                    }
                    // Bright border on stripe
                    g2.setColor(gc.brighter());
                    g2.setStroke(new BasicStroke(1));
                    switch(dir){
                        case 0: g2.drawRect(r.x,r.y,r.width,sw); break;
                        case 1: g2.drawRect(r.x+r.width-sw,r.y,sw,r.height); break;
                        case 2: g2.drawRect(r.x,r.y+r.height-sw,r.width,sw); break;
                        case 3: g2.drawRect(r.x,r.y,sw,r.height); break;
                    }
                }
            }

            // Border & Owner Glow
            Color cellBorder = new Color(BORDER.getRed(),BORDER.getGreen(),BORDER.getBlue(),200);
            float strokeW = 1f;
            if(sq instanceof PropertySquare){
                PropertySquare prop=(PropertySquare)sq;
                if(prop.getOwner()!=null){
                    int pi=pidx(prop.getOwner());
                    if(pi>=0 && pi<P_COL.length){
                        cellBorder = P_COL[pi];
                        strokeW = 2.2f;
                    }
                }
            }
            g2.setColor(cellBorder);
            g2.setStroke(new BasicStroke(strokeW));
            g2.drawRect(r.x,r.y,r.width,r.height);

            // Draw icon + text
            drawCellContent(g2,sq,r,dir);
        }

        private void drawCellContent(Graphics2D g2,Square sq,Rectangle r,int dir){
            if(dir==-1){ drawCorner(g2,sq,r); return; }

            int cx=r.x+r.width/2, cy=r.y+r.height/2;
            if(dir==2){
                drawTopCellContent(g2,sq,r,cx,cy);
                return;
            }

            AffineTransform old=g2.getTransform();
            if(dir==1) g2.rotate(-Math.PI/2,cx,cy);
            else if(dir==3) g2.rotate(Math.PI/2,cx,cy);
            // After rotation treat as dir==0 (bottom): width=visual-width, height=visual-height

            int vw=(dir==0||dir==2)?r.width:r.height;
            int vh=(dir==0||dir==2)?r.height:r.width;

            // Position number top-left
            g2.setFont(new Font("SansSerif",Font.BOLD,8));
            FontMetrics fm=g2.getFontMetrics();
            g2.setColor(C_GOLD);
            String pos=String.valueOf(sq.getPosition());
            g2.drawString(pos,cx-vw/2+2,cy-vh/2+fm.getAscent()+1);

            // Square-type icon in middle area
            int iconY=cy-4;
            int iconR=Math.max(9,Math.min(14,vw/5));
            drawSquareIcon(g2,sq,cx,iconY,iconR,vw,vh);

            // Name text below icon
            String name=shortName(sq.getName());
            int fs=Math.max(7,Math.min(9,vw/7));
            g2.setFont(new Font("SansSerif",Font.PLAIN,fs));
            fm=g2.getFontMetrics();
            g2.setColor(TEXT_HI);
            // split into 2 lines if long
            String[]words=name.split(" ");
            List<String>lines=new ArrayList<>();
            StringBuilder lb=new StringBuilder();
            for(String w:words){
                if(lb.length()+w.length()>10&&lb.length()>0){lines.add(lb.toString().trim());lb=new StringBuilder();}
                lb.append(w).append(" ");
            }
            if(lb.length()>0) lines.add(lb.toString().trim());
            int lh=fm.getHeight();
            int ty=cy+iconR+3;
            for(int i=0;i<Math.min(lines.size(),2);i++){
                String ln=lines.get(i);
                g2.drawString(ln,cx-fm.stringWidth(ln)/2,ty+i*lh);
            }

            // Price for property / Tax for tax square
            if(sq instanceof PropertySquare){
                PropertySquare prop=(PropertySquare)sq;
                g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(7,fs)));
                fm=g2.getFontMetrics();
                g2.setColor(C_TEAL);
                String pr="$"+prop.getPrice();
                g2.drawString(pr,cx-fm.stringWidth(pr)/2,cy+vh/2-3);
            } else if(sq instanceof TaxSquare){
                TaxSquare tx=(TaxSquare)sq;
                g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(7,fs)));
                fm=g2.getFontMetrics();
                g2.setColor(new Color(255,100,100));
                String pr="-$"+tx.getTaxAmount();
                g2.drawString(pr,cx-fm.stringWidth(pr)/2,cy+vh/2-3);
            }

            // KÍ HIỆU NHÀ & CHỦ SỞ HỮU THEO MÀU NGƯỜI CHƠI (Xanh dương, Xanh lá, Đỏ, Vàng)
            if(sq instanceof PropertySquare){
                PropertySquare prop=(PropertySquare)sq;
                if(prop.getOwner()!=null){
                    int pi=pidx(prop.getOwner());
                    Color ownerCol=pi>=0&&pi<P_COL.length?P_COL[pi]:C_GREEN;

                    // 1. Huy hiệu chủ sở hữu (Owner Badge) ở góc trên-phải
                    int bw2=20, bh2=12;
                    int bx2=cx+vw/2-bw2-2, by2c=cy-vh/2+2;
                    g2.setColor(new Color(0,0,0,110));
                    g2.fillRoundRect(bx2+1,by2c+1,bw2,bh2,5,5);
                    g2.setColor(ownerCol);
                    g2.fillRoundRect(bx2,by2c,bw2,bh2,5,5);
                    g2.setColor(new Color(255,255,255,140));
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawRoundRect(bx2,by2c,bw2,bh2,5,5);
                    g2.setColor(getContrastColor(ownerCol));
                    g2.setFont(new Font("SansSerif",Font.BOLD,8));
                    FontMetrics fmb=g2.getFontMetrics();
                    String ini=pi>=0&&pi<P_INITIALS.length?P_INITIALS[pi]:("P"+(pi+1));
                    g2.drawString(ini,bx2+(bw2-fmb.stringWidth(ini))/2,by2c+fmb.getAscent()+1);

                    // 2. Vạch sở hữu màu người chơi dưới dải màu nhóm
                    int sw=Math.max(8,Math.min(12,(dir==-1?r.width:Math.max(r.width,r.height))/7));
                    g2.setColor(ownerCol);
                    g2.fillRect(cx-vw/2+2,cy-vh/2+sw+1,vw-4,2);

                    // 3. Nhà / Khách sạn hoặc Cờ sở hữu hiển thị theo màu người chơi
                    int houseBaseY=iconY-iconR-2;
                    if(prop.getHouseLevel()>0){
                        drawHouses(g2,prop.getHouseLevel(),cx,houseBaseY,vw,ownerCol);
                    }else{
                        drawOwnerFlag(g2,cx,houseBaseY,ownerCol);
                    }
                }
            }

            g2.setTransform(old);
        }

        private void drawTopCellContent(Graphics2D g2,Square sq,Rectangle r,int cx,int cy){
            int sw=Math.max(8,Math.min(12,Math.max(r.width,r.height)/7));

            // Position number top-left
            g2.setFont(new Font("SansSerif",Font.BOLD,8));
            FontMetrics fm=g2.getFontMetrics();
            g2.setColor(C_GOLD);
            String pos=String.valueOf(sq.getPosition());
            g2.drawString(pos,r.x+2,r.y+fm.getAscent()+1);

            PropertySquare prop=(sq instanceof PropertySquare)?(PropertySquare)sq:null;
            Color ownerCol=null;
            int pi=-1;
            if(prop!=null && prop.getOwner()!=null){
                pi=pidx(prop.getOwner());
                ownerCol=pi>=0&&pi<P_COL.length?P_COL[pi]:C_GREEN;

                // 1. Owner Badge top-right
                int bw2=20, bh2=12;
                int bx2=r.x+r.width-bw2-2, by2c=r.y+2;
                g2.setColor(new Color(0,0,0,110));
                g2.fillRoundRect(bx2+1,by2c+1,bw2,bh2,5,5);
                g2.setColor(ownerCol);
                g2.fillRoundRect(bx2,by2c,bw2,bh2,5,5);
                g2.setColor(new Color(255,255,255,140));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(bx2,by2c,bw2,bh2,5,5);
                g2.setColor(getContrastColor(ownerCol));
                g2.setFont(new Font("SansSerif",Font.BOLD,8));
                FontMetrics fmb=g2.getFontMetrics();
                String ini=pi>=0&&pi<P_INITIALS.length?P_INITIALS[pi]:("P"+(pi+1));
                g2.drawString(ini,bx2+(bw2-fmb.stringWidth(ini))/2,by2c+fmb.getAscent()+1);

                // 2. Owner stripe above group color bar
                g2.setColor(ownerCol);
                g2.fillRect(r.x+2,r.y+r.height-sw-2,r.width-4,2);

                // 3. Houses / Flag
                int houseBaseY=r.y+20;
                if(prop.getHouseLevel()>0){
                    drawHouses(g2,prop.getHouseLevel(),cx,houseBaseY,r.width,ownerCol);
                }else{
                    drawOwnerFlag(g2,cx,houseBaseY,ownerCol);
                }
            }

            // Icon
            int iconY = r.y + (prop!=null && prop.getOwner()!=null ? 36 : 28);
            int iconR = Math.max(9, Math.min(14, r.width/5));
            drawSquareIcon(g2, sq, cx, iconY, iconR, r.width, r.height);

            // Name text below icon
            String name=shortName(sq.getName());
            int fs=Math.max(7,Math.min(9,r.width/7));
            g2.setFont(new Font("SansSerif",Font.PLAIN,fs));
            fm=g2.getFontMetrics();
            g2.setColor(TEXT_HI);
            String[]words=name.split(" ");
            List<String>lines=new ArrayList<>();
            StringBuilder lb=new StringBuilder();
            for(String w:words){
                if(lb.length()+w.length()>10&&lb.length()>0){lines.add(lb.toString().trim());lb=new StringBuilder();}
                lb.append(w).append(" ");
            }
            if(lb.length()>0) lines.add(lb.toString().trim());
            int lh=fm.getHeight();
            int ty=iconY+iconR+3;
            for(int i=0;i<Math.min(lines.size(),2);i++){
                String ln=lines.get(i);
                g2.drawString(ln,cx-fm.stringWidth(ln)/2,ty+i*lh);
            }

            // Price / Tax amount
            if(prop!=null){
                g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(7,fs)));
                fm=g2.getFontMetrics();
                g2.setColor(C_TEAL);
                String pr="$"+prop.getPrice();
                g2.drawString(pr,cx-fm.stringWidth(pr)/2,r.y+r.height-sw-4);
            } else if(sq instanceof TaxSquare){
                TaxSquare tx=(TaxSquare)sq;
                g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(7,fs)));
                fm=g2.getFontMetrics();
                g2.setColor(new Color(255,100,100));
                String pr="-$"+tx.getTaxAmount();
                g2.drawString(pr,cx-fm.stringWidth(pr)/2,r.y+r.height-sw-4);
            }
        }

        private void drawCorner(Graphics2D g2,Square sq,Rectangle r){
            int cx=r.x+r.width/2, cy=r.y+r.height/2;
            int pos=sq.getPosition();

            // Position label
            g2.setFont(new Font("SansSerif",Font.BOLD,9));
            g2.setColor(TEXT_DIM);
            FontMetrics fm=g2.getFontMetrics();
            String ps=String.valueOf(pos);
            g2.drawString(ps,r.x+3,r.y+fm.getAscent()+2);

            int maxW = (int)(r.width * 0.72);
            int maxH = (int)(r.height * 0.52);
            int iconCy = cy - 6;

            if(pos==0){
                if(IMG_BAT_DAU != null){
                    drawScaledImage(g2, IMG_BAT_DAU, cx, iconCy, maxW, maxH);
                } else {
                    drawGoIcon(g2, cx, iconCy, Math.max(16, r.width/4));
                }
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, r.width/7)));
                fm = g2.getFontMetrics();
                g2.setColor(C_GREEN);
                g2.drawString("BẮT ĐẦU", cx - fm.stringWidth("BẮT ĐẦU")/2, r.y + r.height - 6);
            } else if(pos==10){
                if(IMG_VAO_TU != null){
                    drawScaledImage(g2, IMG_VAO_TU, cx, iconCy, maxW, maxH);
                } else {
                    drawJailIcon(g2, cx, iconCy, Math.max(16, r.width/4));
                }
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, r.width/8)));
                fm = g2.getFontMetrics();
                g2.setColor(new Color(225, 230, 255));
                g2.drawString("THĂM TÙ", cx - fm.stringWidth("THĂM TÙ")/2, r.y + r.height - 6);
            } else if(pos==20){
                if(IMG_BAI_DO_XE != null){
                    drawScaledImage(g2, IMG_BAI_DO_XE, cx, iconCy, maxW, maxH);
                } else {
                    drawParkingIcon(g2, cx, iconCy, Math.max(16, r.width/4));
                }
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, r.width/8)));
                fm = g2.getFontMetrics();
                g2.setColor(new Color(90, 175, 255));
                g2.drawString("BÃI ĐỖ XE", cx - fm.stringWidth("BÃI ĐỖ XE")/2, r.y + r.height - 6);
            } else if(pos==30){
                if(IMG_VAO_TU != null){
                    drawScaledImage(g2, IMG_VAO_TU, cx, iconCy, maxW, maxH);
                } else {
                    drawGoJailIcon(g2, cx, iconCy, Math.max(16, r.width/4));
                }
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, r.width/8)));
                fm = g2.getFontMetrics();
                g2.setColor(new Color(255, 80, 80));
                g2.drawString("VÀO TÙ", cx - fm.stringWidth("VÀO TÙ")/2, r.y + r.height - 6);
            }
        }

        // ── Square-type icons ────────────────────────────────────

        private void drawSquareIcon(Graphics2D g2,Square sq,int cx,int cy,int r,int vw,int vh){
            int maxW = Math.min(36, vw - 12);
            int maxH = Math.min(28, (int)(vh * 0.32));

            if(sq instanceof RailroadSquare){
                drawTrainIcon(g2,cx,cy,r);
            }
            else if(sq instanceof UtilitySquare){
                drawUtilityIcon(g2,(UtilitySquare)sq,cx,cy,r);
            }
            else if(sq instanceof ChanceSquare){
                if(IMG_CO_HOI != null){
                    drawScaledImage(g2, IMG_CO_HOI, cx, cy, maxW, maxH);
                } else {
                    drawStarIcon(g2, cx, cy, r, C_GOLD, 5);
                }
            }
            else if(sq instanceof CommunityChestSquare){
                if(IMG_CO_HOI != null){
                    drawScaledImage(g2, IMG_CO_HOI, cx, cy, maxW, maxH);
                } else {
                    drawChestIcon(g2, cx, cy, r);
                }
            }
            else if(sq instanceof TaxSquare){
                if(IMG_THUE != null){
                    drawScaledImage(g2, IMG_THUE, cx, cy, maxW, maxH);
                } else {
                    drawTaxIcon(g2, cx, cy, r);
                }
            }
            else if(sq instanceof PropertySquare){
                PropertySquare prop = (PropertySquare) sq;
                if(prop.getHouseLevel() > 0 && IMG_NHA != null){
                    drawScaledImage(g2, IMG_NHA, cx, cy, maxW, maxH);
                }
            }
        }

        // 5-pointed star
        private void drawStar(Graphics2D g2,int cx,int cy,int r,int points,Color c){
            g2.setColor(c);
            int[]xy=starPoly(cx,cy,r,(int)(r*0.42),points);
            int n=xy.length/2; int[]xs=new int[n],ys=new int[n];
            for(int i=0;i<n;i++){xs[i]=xy[i*2];ys[i]=xy[i*2+1];}
            g2.fillPolygon(xs,ys,n);
        }
        private void drawStarIcon(Graphics2D g2,int cx,int cy,int r,Color c,int pts){
            // Glow
            g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),60));
            int[]xy=starPoly(cx,cy,r+3,(int)((r+3)*0.42),pts);
            int n=xy.length/2;int[]xs=new int[n],ys=new int[n];
            for(int i=0;i<n;i++){xs[i]=xy[i*2];ys[i]=xy[i*2+1];}
            g2.fillPolygon(xs,ys,n);
            // Fill
            g2.setColor(c);
            xy=starPoly(cx,cy,r,(int)(r*0.42),pts);
            n=xy.length/2;xs=new int[n];ys=new int[n];
            for(int i=0;i<n;i++){xs[i]=xy[i*2];ys[i]=xy[i*2+1];}
            g2.fillPolygon(xs,ys,n);
            // Outline
            g2.setColor(c.brighter());
            g2.setStroke(new BasicStroke(1));
            g2.drawPolygon(xs,ys,n);
        }
        private int[] starPoly(int cx,int cy,int outer,int inner,int pts){
            int total=pts*2; int[]arr=new int[total*2];
            for(int i=0;i<total;i++){
                double angle=Math.PI/pts*i-Math.PI/2;
                int rad=(i%2==0)?outer:inner;
                arr[i*2]=(int)(cx+rad*Math.cos(angle));
                arr[i*2+1]=(int)(cy+rad*Math.sin(angle));
            }
            return arr;
        }

        // Train icon (simple)
        private void drawTrainIcon(Graphics2D g2,int cx,int cy,int r){
            // Body
            g2.setColor(new Color(80,80,110));
            g2.fillRoundRect(cx-r,cy-r/2,r*2,r,6,6);
            // Chimney
            g2.setColor(new Color(60,60,90));
            g2.fillRect(cx-r/3,cy-r,r/2,r/2);
            // Wheels
            g2.setColor(new Color(50,50,80));
            g2.fillOval(cx-r+2,cy+r/2-4,8,8);
            g2.fillOval(cx+r-10,cy+r/2-4,8,8);
            // Steam
            g2.setColor(new Color(180,190,220,140));
            g2.fillOval(cx-r/3+2,cy-r-6,7,7);
            g2.fillOval(cx-r/3+8,cy-r-8,5,5);
            // Highlight
            g2.setColor(new Color(140,150,200,120));
            g2.fillRoundRect(cx-r+2,cy-r/2+2,r*2-4,r/3,4,4);
        }

        // Utility icon (lightning / water drop)
        private void drawUtilityIcon(Graphics2D g2,UtilitySquare sq,int cx,int cy,int r){
            boolean isElec=sq.getName().contains("Điện");
            if(isElec){
                // Lightning bolt
                int[]xp={cx,cx+r/2,cx-r/4,cx+r,cx,cx-r/2,cx+r/4,cx-r};
                int[]yp={cy-r,cy-r/3,cy-r/3,cy,cy,cy+r/3,cy+r/3,cy};
                // simplified bolt: polygon
                int[]bx={cx+r/4,cx+r/2,cx,cx-r/4,cx-r/2,cx};
                int[]by={cy-r,cy-r/4,cy-r/4,cy+r,cy+r/4,cy+r/4};
                g2.setColor(new Color(255,230,0,200));
                g2.fillPolygon(bx,by,6);
                g2.setColor(C_GOLD);
                g2.setStroke(new BasicStroke(1));
                g2.drawPolygon(bx,by,6);
            } else {
                // Water drop
                int dr=r; int bx2=cx, by=cy+dr/2;
                Path2D drop=new Path2D.Double();
                drop.moveTo(bx2,by2-dr);
                drop.curveTo(bx2+dr,by2-dr/2,bx2+dr,by2+dr/2,bx2,by2+dr/2);
                drop.curveTo(bx2-dr,by2+dr/2,bx2-dr,by2-dr/2,bx2,by2-dr);
                drop.closePath();
                g2.setColor(new Color(60,160,255,200));
                g2.fill(drop);
                g2.setColor(C_BLUE.brighter());
                g2.setStroke(new BasicStroke(1));
                g2.draw(drop);
                // Highlight
                g2.setColor(new Color(255,255,255,80));
                g2.fillOval(bx2-dr/3,by2-dr/2,dr/3,dr/3);
            }
        }

        // Community chest icon (card / chest)
        private void drawChestIcon(Graphics2D g2,int cx,int cy,int r){
            // Card shape
            g2.setColor(new Color(30,80,160));
            g2.fillRoundRect(cx-r,cy-r*3/4,r*2,r*3/2,5,5);
            g2.setColor(C_BLUE.brighter());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cx-r,cy-r*3/4,r*2,r*3/2,5,5);
            // Heart or star inside
            g2.setColor(new Color(255,100,150));
            int hr=r/2;
            // simple heart: two circles + triangle
            g2.fillOval(cx-hr,cy-hr/2,hr,hr);
            g2.fillOval(cx,cy-hr/2,hr,hr);
            int[]hx={cx-hr,cx+hr+1,cx+hr/2};
            int[]hy={cy,cy,cy+hr};
            g2.fillPolygon(hx,hy,3);
        }

        // Tax icon (money bag)
        private void drawTaxIcon(Graphics2D g2,int cx,int cy,int r){
            // Bag body
            g2.setColor(new Color(200,170,50));
            g2.fillOval(cx-r,cy-r/2,r*2,r+r/2);
            // Bag neck
            g2.setColor(new Color(160,130,30));
            g2.fillRoundRect(cx-r/3,cy-r,r*2/3,r/2,4,4);
            // $ sign
            g2.setColor(new Color(100,70,0));
            g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(9,r)));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString("$",cx-fm.stringWidth("$")/2,cy+r/4+fm.getAscent()/2);
        }

        // GO icon
        private void drawGoIcon(Graphics2D g2,int cx,int cy,int r){
            // Circle
            g2.setColor(new Color(30,160,80));
            g2.fillOval(cx-r,cy-r,r*2,r*2);
            g2.setColor(C_GREEN.brighter());
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx-r,cy-r,r*2,r*2);
            // Arrow
            g2.setColor(Color.WHITE);
            int aw=r-4;
            g2.fillPolygon(new int[]{cx-aw/2,cx+aw/2,cx+aw/2,cx+aw,cx,cx-aw},
                           new int[]{cy+aw/4,cy+aw/4,cy+aw/2,cy,cy-aw/2,cy},6);
        }

        // Jail icon
        private void drawJailIcon(Graphics2D g2,int cx,int cy,int r){
            // Background cell
            g2.setColor(new Color(60,40,80));
            g2.fillRoundRect(cx-r,cy-r,r*2,r*2,6,6);
            // Bars
            g2.setColor(new Color(180,180,220));
            g2.setStroke(new BasicStroke(2.5f));
            int nb=4, gap=r*2/(nb+1);
            for(int i=1;i<=nb;i++){
                int bx=cx-r+i*gap;
                g2.drawLine(bx,cy-r+4,bx,cy+r-4);
            }
            // Horizontal bar
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(cx-r+4,cy,cx+r-4,cy);
            // Person head
            g2.setColor(new Color(255,220,170));
            g2.fillOval(cx-4,cy-r+6,8,8);
        }

        // Free parking
        private void drawParkingIcon(Graphics2D g2,int cx,int cy,int r){
            g2.setColor(new Color(30,80,160));
            g2.fillOval(cx-r,cy-r,r*2,r*2);
            g2.setColor(C_BLUE.brighter());
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx-r,cy-r,r*2,r*2);
            g2.setFont(new Font("SansSerif",Font.BOLD,r));
            g2.setColor(Color.WHITE);
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString("P",cx-fm.stringWidth("P")/2,cy+fm.getAscent()/2);
        }

        // Go to jail icon
        private void drawGoJailIcon(Graphics2D g2,int cx,int cy,int r){
            // Red arrow pointing to bars
            g2.setColor(C_RED);
            g2.fillPolygon(new int[]{cx-r/2,cx,cx+r/2,cx+r,cx,cx-r},
                           new int[]{cy+r/4,cy-r/2,cy+r/4,cy,cy+r/2,cy},6);
            // Bars on right
            g2.setColor(new Color(180,180,220));
            g2.setStroke(new BasicStroke(2f));
            for(int i=0;i<3;i++) g2.drawLine(cx+r/2+i*4,cy-r/2,cx+r/2+i*4,cy+r/2);
        }

        // ── Houses / Hotel / Flag – màu theo chủ sở hữu ────────
        private void drawHouses(Graphics2D g2,int level,int cx,int baseY,int maxW,Color ownerCol){
            if(level<=0) return;
            if(level==5){
                drawHotel(g2,cx,baseY,Math.max(20,maxW/3),ownerCol);
                return;
            }
            // 1–4 houses
            int houseW=Math.max(8,Math.min(14,(maxW-4)/(level+1)));
            int houseH=(int)(houseW*1.1);
            int totalW=level*(houseW+2)-2;
            int startX=cx-totalW/2;
            for(int i=0;i<level;i++){
                int hx=startX+i*(houseW+2);
                drawHouseShape(g2,hx,baseY,houseW,houseH,level,ownerCol);
            }
        }

        /** Cờ sở hữu mini khi đã mua nhưng chưa xây nhà */
        private void drawOwnerFlag(Graphics2D g2,int cx,int baseY,Color ownerCol){
            int poleH=11;
            int px=cx-4;
            int py=baseY-poleH;
            // Cột cờ
            g2.setColor(new Color(200,215,235));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(px,py,px,baseY);
            // Núm vàng đỉnh cột
            g2.setColor(C_GOLD);
            g2.fillOval(px-1,py-2,3,3);
            // Lá cờ tam giác theo màu người chơi
            int[] fx={px, px+9, px};
            int[] fy={py, py+4, py+8};
            g2.setColor(ownerCol);
            g2.fillPolygon(fx,fy,3);
            g2.setColor(new Color(0,0,0,90));
            g2.setStroke(new BasicStroke(0.6f));
            g2.drawPolygon(fx,fy,3);
        }

        private void drawHouseShape(Graphics2D g2,int hx,int baseY,int w,int h,int level,Color ownerCol){
            // Tường = màu người chơi, mái = tối hơn
            Color wallC=ownerCol;
            Color roofC=ownerCol.darker().darker();
            if(level>=3) wallC=ownerCol.brighter();
            // Drop shadow
            g2.setColor(new Color(0,0,0,70));
            g2.fillRect(hx+1,baseY-h/2+1,w,h/2);
            // Wall
            g2.setColor(wallC);
            g2.fillRect(hx,baseY-h/2,w,h/2);
            // Wall shine
            g2.setColor(new Color(255,255,255,60));
            g2.fillRect(hx+1,baseY-h/2+1,w/2,h/4);
            // Roof (triangle)
            int[]rx={hx-1,hx+w/2,hx+w+1};
            int[]ry={baseY-h/2,baseY-h,baseY-h/2};
            g2.setColor(roofC);
            g2.fillPolygon(rx,ry,3);
            // Roof shine
            g2.setColor(new Color(255,255,255,50));
            g2.fillPolygon(new int[]{rx[0]+1,hx+w/2,hx+w/3},new int[]{ry[0],ry[1]+2,ry[0]},3);
            // Window
            g2.setColor(new Color(240,248,255,220));
            g2.fillRect(hx+w/2-1,baseY-h/2+2,2,2);
            // Outline
            g2.setColor(new Color(0,0,0,90));
            g2.setStroke(new BasicStroke(0.5f));
            g2.drawRect(hx,baseY-h/2,w,h/2);
            g2.drawPolygon(rx,ry,3);
        }

        private void drawHotel(Graphics2D g2,int cx,int baseY,int w,Color ownerCol){
            int h=(int)(w*1.35);
            Color wall=ownerCol;
            Color roof=ownerCol.darker().darker();
            // Drop shadow
            g2.setColor(new Color(0,0,0,90));
            g2.fillRoundRect(cx-w/2+1,baseY-h+1,w,h,3,3);
            // Main building
            g2.setColor(wall);
            g2.fillRoundRect(cx-w/2,baseY-h,w,h,3,3);
            // Building shine
            g2.setColor(new Color(255,255,255,65));
            g2.fillRoundRect(cx-w/2+2,baseY-h+2,w/2,h/3,3,3);
            // Roof ledge
            g2.setColor(roof);
            g2.fillRect(cx-w/2-2,baseY-h,w+4,Math.max(3,h/5));
            // Windows grid
            g2.setColor(new Color(240,248,255,220));
            int rows=3, cols=3;
            int ww=Math.max(2,(w-4)/cols/2), wh=Math.max(2,(h-h/5-4)/rows/2);
            for(int r=0;r<rows;r++) for(int c=0;c<cols;c++){
                g2.fillRect(cx-w/2+2+c*(w/cols),baseY-h+h/5+2+r*(h/rows/2+2),ww,wh);
            }
            // "H" label
            g2.setColor(getContrastColor(wall));
            g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(9,w/3)));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString("H",cx-fm.stringWidth("H")/2,baseY-h/2+fm.getAscent()/2);
            // Outline
            g2.setColor(new Color(0,0,0,100));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(cx-w/2,baseY-h,w,h,3,3);
        }

        // ── Player tokens (cartoon) ──────────────────────────────
        private void drawTokens(Graphics2D g2,int x0,int y0,int sz){
            int[]d=dims(sz); int corner=d[0],cell=d[1];
            Player[]ps=engine.getPlayers();
            int[]cnt=new int[40]; int[]slot=new int[ps.length];
            for(int i=0;i<ps.length;i++){
                if(ps[i].isBankrupt())continue;
                int pos=(animPidx==i)?animCurPos:ps[i].getPosition();
                slot[i]=cnt[pos]++;
            }
            for(int i=0;i<ps.length;i++){
                if(ps[i].isBankrupt())continue;
                int pos=(animPidx==i)?animCurPos:ps[i].getPosition();
                Rectangle r = cellRect(pos, x0, y0, corner, cell);
                int tokenSize = 28;
                int offX = (slot[i] % 2) * 16 - 8;
                int offY = (slot[i] / 2) * 18 - 9;
                int tx = r.x + r.width / 2 + offX - tokenSize / 2;
                int ty = r.y + r.height / 2 + offY - tokenSize / 2;
                drawCartoonToken(g2, i, tx, ty, tokenSize);
            }
        }

        /**
         * Draws a cute cartoon character token for player idx at (tx,ty) with total height ~h.
         * Structure: body (oval) + head (circle) + eyes + mouth + hair
         */
        private void drawCartoonToken(Graphics2D g2,int idx,int tx,int ty,int h){
            int bw=(int)(h*0.7), bh=(int)(h*0.5);
            int headR=(int)(h*0.38);
            int bodyY=ty+headR*2-4;
            int bodyX=tx+(h-bw)/2;

            // Drop shadow
            g2.setColor(new Color(0,0,0,80));
            g2.fillOval(tx+2,ty+h-4,h-2,5);

            // Body
            Color body=P_COL[idx];
            g2.setColor(body);
            g2.fillOval(bodyX,bodyY,bw,bh);
            // Body shine
            g2.setColor(new Color(255,255,255,60));
            g2.fillOval(bodyX+bw/4,bodyY+2,bw/3,bh/3);

            // Head
            int headCx=tx+h/2, headCy=ty+headR+2;
            // Head shadow
            g2.setColor(body.darker());
            g2.fillOval(headCx-headR-1,headCy-headR+1,headR*2,headR*2);
            // Head fill
            g2.setColor(new Color(255,220,180)); // skin tone
            g2.fillOval(headCx-headR,headCy-headR,headR*2,headR*2);

            // Hair / hat on top
            Color hair=P_HAIR[idx];
            g2.setColor(hair);
            // Simple spiked hair: arc + spikes
            g2.fillArc(headCx-headR,headCy-headR,headR*2,headR*2,0,180);
            // Two spikes
            int[]sx={headCx-headR/2-2,headCx-headR/2+3,headCx-headR/4};
            int[]sy={headCy-headR+2,headCy-headR+2,headCy-headR-headR/2};
            g2.fillPolygon(sx,sy,3);
            int[]sx2={headCx+headR/4,headCx+headR/2+2,headCx+headR/2-3};
            int[]sy2={headCy-headR-headR/3,headCy-headR+2,headCy-headR+2};
            g2.fillPolygon(sx2,sy2,3);

            // Eyes (white + pupil)
            int eyeR=Math.max(2,headR/3);
            int ley=headCy-2;
            // Left eye
            g2.setColor(Color.WHITE);
            g2.fillOval(headCx-headR/2-eyeR/2,ley-eyeR/2,eyeR,eyeR);
            g2.setColor(new Color(30,20,10));
            g2.fillOval(headCx-headR/2-eyeR/2+1,ley-eyeR/2+1,eyeR/2,eyeR/2);
            // Right eye
            g2.setColor(Color.WHITE);
            g2.fillOval(headCx+headR/2-eyeR/2,ley-eyeR/2,eyeR,eyeR);
            g2.setColor(new Color(30,20,10));
            g2.fillOval(headCx+headR/2-eyeR/2+1,ley-eyeR/2+1,eyeR/2,eyeR/2);

            // Smile
            g2.setColor(new Color(180,60,60));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawArc(headCx-headR/3,ley+2,headR*2/3,headR/3,0,-180);

            // Blush circles
            g2.setColor(new Color(255,150,150,100));
            g2.fillOval(headCx-headR+1,ley+1,headR/2,headR/3);
            g2.fillOval(headCx+headR/2,ley+1,headR/2,headR/3);

            // Head outline
            g2.setColor(new Color(0,0,0,80));
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(headCx-headR,headCy-headR,headR*2,headR*2);

            // Player initial badge
            g2.setColor(body);
            g2.fillOval(bodyX+bw-9,bodyY-4,12,12);
            g2.setColor(new Color(0,0,0,80));
            g2.setStroke(new BasicStroke(0.6f));
            g2.drawOval(bodyX+bw-9,bodyY-4,12,12);
            g2.setColor(getContrastColor(body));
            g2.setFont(new Font("SansSerif",Font.BOLD,7));
            FontMetrics fm=g2.getFontMetrics();
            String ini=String.valueOf(idx+1);
            g2.drawString(ini,bodyX+bw-9+(12-fm.stringWidth(ini))/2,bodyY-4+9);
        }

        // ── Cell colors ──────────────────────────────────────────
        private Color cellBg1(Square sq){
            if(sq instanceof JailSquare)          return new Color(30,25,48);
            if(sq instanceof TaxSquare)           return new Color(50,18,18);
            if(sq instanceof ChanceSquare)        return new Color(50,38,12);
            if(sq instanceof CommunityChestSquare)return new Color(12,34,58);
            if(sq instanceof UtilitySquare)       return new Color(22,44,36);
            if(sq instanceof RailroadSquare)      return new Color(22,28,50);
            if(sq instanceof SpecialSquare)       return new Color(14,44,30);
            return new Color(20,28,54);
        }
        private Color cellBg2(Square sq){
            Color c=cellBg1(sq);
            return new Color(Math.min(255,c.getRed()+12),Math.min(255,c.getGreen()+14),Math.min(255,c.getBlue()+18));
        }
        private Color groupColor(PropertySquare p){
            ColorGroup g=p.getColorGroup(); if(g==null)return null;
            switch(g){
                case PURPLE:    return GROUP_C[0];
                case LIGHT_BLUE:return GROUP_C[1];
                case PINK:      return GROUP_C[2];
                case ORANGE:    return GROUP_C[3];
                case RED:       return GROUP_C[4];
                case YELLOW:    return GROUP_C[5];
                case GREEN:     return GROUP_C[6];
                case DARK_BLUE: return GROUP_C[7];
                default: return null;
            }
        }
        private String shortName(String name){
            name=name.replace("Đường ","").replace("Ga ","").replace("Công Ty ","").replace(" Miễn Phí","");
            return name.length()>16?name.substring(0,15)+"…":name;
        }
    }

    // ══════════ WATER DROP FIX (nested var) ══════════════════════
    // Helper for water drop in utility icon
    private static int by2=0; // used as mutable in drawUtilityIcon – use drawPath instead below

    // ══════════ DICE FACE ═════════════════════════════════════════
    class DiceFace extends JPanel {
        private int val=1; private boolean anim=false;
        DiceFace(){ setPreferredSize(new Dimension(58,58)); setOpaque(false); }
        void setValue(int v){val=Math.max(1,Math.min(6,v));}
        void setAnim(boolean a){anim=a;}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int s=Math.min(getWidth(),getHeight())-4;
            int ox=(getWidth()-s)/2, oy=(getHeight()-s)/2;
            // Shadow
            g2.setColor(new Color(0,0,0,80));
            g2.fillRoundRect(ox+3,oy+3,s,s,12,12);
            // Body
            if(anim) g2.setPaint(new GradientPaint(ox,oy,new Color(55,65,105),ox+s,oy+s,new Color(35,45,80)));
            else     g2.setPaint(new GradientPaint(ox,oy,new Color(235,238,255),ox+s,oy+s,new Color(175,180,230)));
            g2.fillRoundRect(ox,oy,s,s,12,12);
            // Border
            g2.setColor(anim?new Color(80,100,180):new Color(90,100,165));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(ox,oy,s,s,12,12);
            // Gloss
            g2.setColor(new Color(255,255,255,anim?40:90));
            g2.fillRoundRect(ox+3,oy+3,s-6,s/3,8,8);
            // Dots
            Color dc=anim?new Color(160,175,255):new Color(20,30,85);
            g2.setColor(dc);
            int dr=s/9;
            for(int[]p:dots(val,ox,oy,s)) g2.fillOval(p[0]-dr,p[1]-dr,dr*2,dr*2);
            g2.dispose();
        }
        private int[][] dots(int v,int ox,int oy,int s){
            int q=s/4,h=s/2;
            int l=ox+q,c=ox+h,r=ox+3*q,t=oy+q,m=oy+h,b=oy+3*q;
            switch(v){
                case 1:return new int[][]{{c,m}};
                case 2:return new int[][]{{l,t},{r,b}};
                case 3:return new int[][]{{l,t},{c,m},{r,b}};
                case 4:return new int[][]{{l,t},{r,t},{l,b},{r,b}};
                case 5:return new int[][]{{l,t},{r,t},{c,m},{l,b},{r,b}};
                default:return new int[][]{{l,t},{r,t},{l,m},{r,m},{l,b},{r,b}};
            }
        }
    }

    // ══════════ HELPER COMPONENTS ════════════════════════════════
    static class GradPanel extends JPanel {
        private final Color c1,c2;
        GradPanel(Color a,Color b){c1=a;c2=b;setOpaque(false);}
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setPaint(new GradientPaint(0,0,c1,0,getHeight(),c2));
            g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
        }
    }

    /** Simple cartoon avatar thumbnail for player card */
    class PlayerAvatar extends JPanel {
        private final int idx;
        PlayerAvatar(int i,int size){idx=i;setPreferredSize(new Dimension(size+4,size+4));setOpaque(false);}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int h=getHeight()-4;
            // Reuse board token drawing
            boardPanel.drawCartoonToken(g2,idx,2,2,h);
            g2.dispose();
        }
    }

    // ══════════ STYLE HELPERS ════════════════════════════════════
    private JLabel lbl(String t,int sz,int style,Color c){ JLabel l=new JLabel(t); l.setFont(new Font("SansSerif",style,sz)); l.setForeground(c); return l; }
    private JPanel panel(Color bg,Color brd,int r){ JPanel p=new JPanel(); p.setBackground(bg); p.setBorder(BorderFactory.createLineBorder(brd,1)); return p; }

    private JButton btn(String text,Color accent,Color bgHint){
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(!isEnabled()) g2.setColor(new Color(32,38,58));
                else if(getModel().isPressed()) g2.setColor(accent.darker().darker());
                else if(getModel().isRollover()) g2.setPaint(new GradientPaint(0,0,accent.brighter(),0,getHeight(),accent));
                else g2.setPaint(new GradientPaint(0,0,accent,0,getHeight(),accent.darker()));
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(new Color(0,0,0,55)); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setFont(new Font("SansSerif",Font.BOLD,13));
        double lum=0.299*accent.getRed()+0.587*accent.getGreen()+0.114*accent.getBlue();
        b.setForeground(lum>140?new Color(14,20,40):TEXT_HI);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110,36));
        return b;
    }
    private void styleTextField(JTextField f){ f.setBackground(BG_CARD);f.setForeground(TEXT_HI);f.setCaretColor(C_GOLD);f.setFont(new Font("SansSerif",Font.PLAIN,14));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),BorderFactory.createEmptyBorder(6,10,6,10))); }
    private void styleSpinner(JSpinner sp){ sp.setBackground(BG_CARD); JComponent ed=sp.getEditor(); if(ed instanceof JSpinner.DefaultEditor){JTextField tf=((JSpinner.DefaultEditor)ed).getTextField();tf.setBackground(BG_CARD);tf.setForeground(TEXT_HI);tf.setCaretColor(C_GOLD);tf.setHorizontalAlignment(JTextField.CENTER);} }

    // ══════════ MAIN ══════════════════════════════════════════════
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            try{UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}catch(Exception ignored){}
            new GameUI();
        });
    }
}
