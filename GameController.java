package ProjectOop;

import ProjectOop.board.Board;
import ProjectOop.dice.Dice;
import ProjectOop.player.Player;
import ProjectOop.square.*;

import java.util.Scanner;

/**
 * Điều phối toàn bộ trò chơi Cờ Tỷ Phú:
 *   - Thiết lập người chơi
 *   - Vòng lặp game (game loop)
 *   - Xử lý lượt đi, tù, mua đất, nâng cấp, phá sản
 */
public class GameController {

    private Board board;
    private Player[] players;
    private Dice dice1, dice2;
    private Scanner scanner;

    public GameController() {
        board  = new Board();
        dice1  = new Dice();
        dice2  = new Dice();
        scanner = new Scanner(System.in);
    }

    // =========================================================
    // ENTRY POINT
    // =========================================================
    public static void main(String[] args) {
        new GameController().run();
    }

    // =========================================================
    // 1. KHỞI ĐỘNG GAME
    // =========================================================
    private void run() {
        printBanner();
        setupPlayers();
        gameLoop();
        announceWinner();
        scanner.close();
    }

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      🎲  CỜ TỶ PHÚ – MONOPOLY 🎲     ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    private void setupPlayers() {
        System.out.print("Nhập số người chơi (2–4): ");
        int count = readInt(2, 4);
        players = new Player[count];
        for (int i = 0; i < count; i++) {
            System.out.print("Tên người chơi " + (i + 1) + ": ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = "Người chơi " + (i + 1);
            players[i] = new Player(name);
        }
        System.out.println("\nBắt đầu! Mỗi người bắt đầu với $" + players[0].getBalance() + ".\n");
    }

    // =========================================================
    // 2. VÒNG LẶP GAME
    // =========================================================
    private void gameLoop() {
        while (countActivePlayers() > 1) {
            for (Player p : players) {
                if (p.isBankrupt()) continue;
                playTurn(p);
                if (countActivePlayers() == 1) return; // Chỉ còn 1 người → kết thúc
            }
        }
    }

    // =========================================================
    // 3. LƯỢT ĐI CỦA MỘT NGƯỜI CHƠI
    // =========================================================
    private void playTurn(Player p) {
        System.out.println("\n════════════════════════════════════════");
        System.out.printf("Lượt: %-15s | Ô: %2d | Tiền: $%d%n",
                p.getName(), p.getPosition(), p.getBalance());
        System.out.println("════════════════════════════════════════");
        waitEnter("Nhấn Enter để đổ xúc xắc...");

        int consecutiveDoubles = 0;
        boolean keepRolling    = true;

        while (keepRolling) {
            keepRolling = false;
            boolean freedFromJailThisTurn = false;

            // --- Xử lý tù ---
            if (p.isInJail()) {
                freedFromJailThisTurn = handleJailTurn(p);
                if (!freedFromJailThisTurn) return; // Vẫn bị tù, kết thúc lượt
            }

            // --- Đổ xúc xắc ---
            int r1    = dice1.roll();
            int r2    = dice2.roll();
            int total = r1 + r2;
            boolean isDouble = (r1 == r2);

            System.out.printf("🎲 Xúc xắc: [%d] + [%d] = %d%s%n",
                    r1, r2, total, isDouble ? "  ⭐ ĐÔI!" : "");

            // --- Đang tù và chọn thử đổ đôi ---
            if (p.isInJail()) {
                p.setTurnsInJail(p.getTurnsInJail() + 1);
                if (isDouble) {
                    System.out.println("Ra tù bằng đôi! Đi " + total + " bước.");
                    p.setInJail(false);
                    p.setTurnsInJail(0);
                    freedFromJailThisTurn = true;
                } else if (p.getTurnsInJail() >= 3) {
                    System.out.println("Hết 3 lượt! Bắt buộc nộp $" + JailSquare.BAIL_AMOUNT + " để ra tù.");
                    if (!p.deductMoney(JailSquare.BAIL_AMOUNT)) {
                        p.deductMoney(p.getBalance());
                        p.setBankrupt(true);
                        handleBankruptcy(p);
                        return;
                    }
                    p.setInJail(false);
                    p.setTurnsInJail(0);
                    freedFromJailThisTurn = true;
                } else {
                    System.out.printf("Không ra đôi. Còn %d lượt thử.%n", 3 - p.getTurnsInJail());
                    return; // Kết thúc lượt, vẫn ở tù
                }
            }

            // --- Kiểm tra 3 đôi liên tiếp (chỉ khi không vừa thoát tù) ---
            if (isDouble && !freedFromJailThisTurn) {
                consecutiveDoubles++;
                if (consecutiveDoubles >= 3) {
                    System.out.println("⚠ Đổ đôi 3 lần liên tiếp! " + p.getName() + " bị vào tù!");
                    sendToJail(p);
                    return;
                }
            }

            // --- Di chuyển ---
            p.setLastDiceRoll(total);
            p.move(total);

            // --- Áp dụng hiệu ứng ô tại vị trí dừng ---
            applySquareAtCurrentPosition(p);

            if (p.isBankrupt()) {
                handleBankruptcy(p);
                return;
            }

            // --- In bảng tài sản nhanh ---
            board.printOwnedProperties();

            // --- Đổ đôi → được đi thêm ---
            if (isDouble && !freedFromJailThisTurn && !p.isInJail()) {
                System.out.println("✅ Đổ đôi! " + p.getName() + " được đi thêm lượt.");
                waitEnter("Nhấn Enter để tiếp tục...");
                keepRolling = true;
            }
        }
    }

    // =========================================================
    // 4. XỬ LÝ TÙ
    // =========================================================
    /**
     * @return true nếu người chơi thoát tù ngay trong lượt này (trước khi đổ xúc xắc)
     */
    private boolean handleJailTurn(Player p) {
        System.out.println("🔒 " + p.getName() + " đang ở tù (lượt tù thứ " + (p.getTurnsInJail() + 1) + "/3).");
        System.out.println("Chọn hành động:");
        System.out.println("  1. Nộp $" + JailSquare.BAIL_AMOUNT + " tiền bảo lãnh");
        if (p.getGetOutOfJailTicket() > 0)
            System.out.println("  2. Dùng thẻ Ra Tù Miễn Phí (đang có " + p.getGetOutOfJailTicket() + " thẻ)");
        System.out.println("  3. Thử đổ đôi để thoát tù");

        int choice = readInt(1, 3);

        if (choice == 1) {
            if (p.deductMoney(JailSquare.BAIL_AMOUNT)) {
                p.setInJail(false);
                p.setTurnsInJail(0);
                System.out.println("Đã nộp $" + JailSquare.BAIL_AMOUNT + ". Ra tù!");
                return true;
            } else {
                System.out.println("Không đủ tiền! Tự động thử đổ đôi.");
            }
        } else if (choice == 2) {
            if (p.useGetOutOfJailTicket()) {
                p.setInJail(false);
                p.setTurnsInJail(0);
                System.out.println("Dùng thẻ Ra Tù Miễn Phí. Thoát tù!");
                return true;
            } else {
                System.out.println("Không có thẻ! Tự động thử đổ đôi.");
            }
        }
        // choice == 3 hoặc fallback: tiếp tục đổ xúc xắc ở phần jail roll
        return false;
    }

    private void sendToJail(Player p) {
        p.setPosition(JailSquare.JAIL_POSITION);
        p.setInJail(true);
        p.setTurnsInJail(0);
    }

    // =========================================================
    // 5. MUA VÀ NÂNG CẤP ĐẤT
    // =========================================================
    private void applySquareAtCurrentPosition(Player p) {
        Square sq = board.getSquare(p.getPosition());
        System.out.println("→ Dừng tại ô " + p.getPosition() + ": " + sq.getName());

        int posBefore = p.getPosition();
        sq.applyEffect(p);
        board.updateColorGroups();

        if (p.isBankrupt()) return;

        int posAfter = p.getPosition();
        if (posAfter != posBefore) {
            System.out.println("→ Dịch chuyển từ ô " + posBefore + " đến ô " + posAfter + ": " + board.getSquare(posAfter).getName());
            if (p.isInJail()) {
                System.out.println("🔒 Bị tống vào tù!");
                return;
            }
            applySquareAtCurrentPosition(p);
            return;
        }

        Square finalSq = board.getSquare(p.getPosition());
        if (finalSq instanceof PropertySquare) {
            PropertySquare prop = (PropertySquare) finalSq;
            if (prop.getOwner() == null) {
                handleBuyDecision(p, prop);
            } else if (prop.getOwner() == p) {
                handleCurrentSquareUpgrade(p, prop);
            }
        }
    }

    private void handleBuyDecision(Player p, PropertySquare prop) {
        if (prop.getOwner() != null) return; // Đã có chủ

        String lvlNote = prop.getHouseCost() > 0 ? " (sau khi mua mặc định là Cấp 1)" : "";
        System.out.printf("%s có thể mua \"%s\" với giá $%d (tiền hiện có: $%d)%s%n",
                p.getName(), prop.getName(), prop.getPrice(), p.getBalance(), lvlNote);

        if (p.getBalance() < prop.getPrice()) {
            System.out.println("Không đủ tiền để mua.");
            return;
        }

        System.out.print("Mua ô đất này? (1=Có / 2=Không): ");
        int choice = readInt(1, 2);
        if (choice == 1) {
            if (prop.buyProperty(p)) {
                String extra = prop.getHouseCost() > 0 ? " (Mặc định Cấp 1 🏠)" : "";
                System.out.println("✅ Mua thành công! Còn $" + p.getBalance() + extra);
                board.updateColorGroups();
            }
        }
    }

    private void handleCurrentSquareUpgrade(Player p, PropertySquare prop) {
        if (!prop.canUpgrade(p)) {
            if (prop.getHouseCost() > 0) {
                if (prop.getHouseLevel() >= 5) {
                    System.out.println("🏨 " + prop.getName() + " đã đạt cấp tối đa (Khách sạn).");
                } else if (p.getBalance() < prop.getHouseCost()) {
                    System.out.println("💸 Bạn không đủ $" + prop.getHouseCost() + " để nâng cấp ô này.");
                }
            }
            return;
        }

        String curLevel = prop.getHouseLevel() == 0 ? "Đất trống" : ("Cấp " + prop.getHouseLevel() + " 🏠");
        String nextLevel = prop.getHouseLevel() == 4 ? "Khách sạn 🏨" : ("Cấp " + (prop.getHouseLevel() + 1) + " 🏠");
        System.out.printf("\n%s đang vào ô đất của mình: \"%s\" (hiện tại: %s)%n", p.getName(), prop.getName(), curLevel);
        System.out.printf("Bạn có muốn nâng cấp lên %s với giá $%d? (1=Có / 2=Không): ", nextLevel, prop.getHouseCost());
        int choice = readInt(1, 2);
        if (choice == 1) {
            if (prop.upgrade(p)) {
                System.out.println("✅ Nâng cấp thành công! " + prop.getName() + " → " + nextLevel + " (Còn $" + p.getBalance() + ")");
            }
        } else {
            System.out.println("Bỏ qua nâng cấp lượt này.");
        }
    }

    // =========================================================
    // 6. PHÁ SẢN
    // =========================================================
    private void handleBankruptcy(Player p) {
        Player creditor = p.getCreditor();

        if (creditor != null) {
            // ── Nợ người chơi khác: chuyển giao toàn bộ tài sản cho chủ nợ ──
            System.out.println("💸 " + p.getName() + " phá sản! Toàn bộ tài sản chuyển cho " + creditor.getName() + ".");
            // Tiền mặt còn lại → chủ nợ
            creditor.addMoney(p.getBalance());
            p.deductMoney(p.getBalance());
            // Đất đai → chủ nợ (nhà bị xóa theo luật khi chuyển giao phá sản)
            for (PropertySquare prop : p.getOwnedProperties()) {
                prop.transferPropertyTo(creditor);
            }
        } else {
            // ── Nợ ngân hàng (thuế, thẻ bài): trả tài sản về ngân hàng ──
            System.out.println("💸 " + p.getName() + " phá sản! Toàn bộ tài sản trả về ngân hàng.");
            for (PropertySquare prop : p.getOwnedProperties()) {
                prop.releaseProperty();
            }
        }

        p.getOwnedProperties().clear();
        board.updateColorGroups();
        System.out.println(p.getName() + " đã bị loại khỏi cuộc chơi.");
    }

    // =========================================================
    // 7. KẾT THÚC GAME
    // =========================================================
    private void announceWinner() {
        System.out.println("\n╔══════════════════════════════════════╗");
        for (Player p : players) {
            if (!p.isBankrupt()) {
                System.out.println("║   🏆  " + p.getName() + " THẮNG CUỘC!  🏆");
                break;
            }
        }
        System.out.println("╚══════════════════════════════════════╝");
    }

    private int countActivePlayers() {
        int count = 0;
        for (Player p : players) if (!p.isBankrupt()) count++;
        return count;
    }

    // =========================================================
    // 8. HÀM TIỆN ÍCH NHẬP LIỆU
    // =========================================================
    private int readInt(int min, int max) {
        while (true) {
            try {
                System.out.printf("(Nhập %d–%d): ", min, max);
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.println("Vui lòng nhập số từ " + min + " đến " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Không hợp lệ. Vui lòng nhập số.");
            }
        }
    }

    private void waitEnter(String message) {
        System.out.print(message);
        scanner.nextLine();
    }
}
