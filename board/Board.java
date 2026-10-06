package ProjectOop.board;

import ProjectOop.card.*;
import ProjectOop.player.Player;
import ProjectOop.square.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bàn Cờ – khởi tạo đầy đủ 40 ô theo chuẩn Monopoly (Việt hóa tên đường).
 * Cung cấp các hàm tiện ích:
 *   - getSquare(int)           : lấy ô theo vị trí
 *   - hasColorGroup(Player, ColorGroup): kiểm tra chủ đã đủ bộ màu chưa
 *   - updateColorGroups()      : cập nhật cờ colorGroupComplete cho toàn bàn
 */
public class Board {
    public static final int SIZE = 40;
    private Square[] squares = new Square[SIZE];

    private CardDeck chanceDeck;
    private CardDeck communityChestDeck;

    public Board() {
        initializeCardDecks();
        initializeSquares();
    }

    // =========================================================
    // 1. KHỞI TẠO BỘ BÀI
    // =========================================================
    private void initializeCardDecks() {
        // --- Bộ bài Cơ Hội (Chance) ---
        List<Card> chanceCards = new ArrayList<>(Arrays.asList(
            new MoveToCard("Tiến đến ô GO. Thu $200.", 0, true),
            new MoveToCard("Tiến đến Võ Văn Tần (ô 24).", 24, true),
            new MoveToCard("Tiến đến Lê Lợi (ô 11).", 11, true),
            new MoveToNearestRailroadCard("Tiến đến Ga gần nhất. Nhận $200 nếu qua ô GO."),
            new JailCard("Vào tù ngay lập tức. Không qua GO, không nhận $200."),
            new MoneyCard("Ngân hàng trả lỗi thuế. Nhận $150.", 150),
            new MoneyCard("Nộp phí sửa đường $50.", -50),
            new MoneyCard("Nhận tiền thưởng kỳ nghỉ $100.", 100),
            new MoneyCard("Nộp phí trường học $150.", -150),
            new GetOutOfJailCard("Thẻ Miễn Phí Ra Tù. Giữ đến khi cần."),
            new MoveRelativeCard("Đi lùi 3 ô.", -3),
            new MoneyCard("Mỗi người chơi trả bạn $50 (đã gộp).", 50),
            new RepairsCard("Sửa chữa: $25/nhà, $100/khách sạn.", 25, 100),
            new MoneyCard("Nhận cổ tức $50.", 50),
            new MoveToCard("Tiến đến Dinh Thống Nhất (ô 39).", 39, true),
            new MoneyCard("Ngân hàng trả bạn $200.", 200)
        ));
        chanceDeck = new CardDeck(chanceCards);

        // --- Bộ bài Khí Vận (Community Chest) ---
        List<Card> communityCards = new ArrayList<>(Arrays.asList(
            new MoveToCard("Tiến đến ô GO. Thu $200.", 0, true),
            new MoneyCard("Nhận tiền thưởng $200.", 200),
            new MoneyCard("Nộp phí bác sĩ $50.", -50),
            new MoneyCard("Nhận lãi quỹ tiết kiệm $50.", 50),
            new JailCard("Vào tù ngay lập tức."),
            new GetOutOfJailCard("Thẻ Miễn Phí Ra Tù."),
            new MoneyCard("Nhận di sản thừa kế $100.", 100),
            new MoneyCard("Nộp phí bệnh viện $100.", -100),
            new MoneyCard("Hoàn thuế thu nhập $20.", 20),
            new MoneyCard("Nhận tiền bảo hiểm nhân thọ $100.", 100),
            new MoneyCard("Nộp phí học bổng $50.", -50),
            new MoneyCard("Nhận tiền tư vấn $25.", 25),
            new RepairsCard("Sửa chữa: $40/nhà, $115/khách sạn.", 40, 115),
            new MoneyCard("Giải nhì cuộc thi sắc đẹp. Nhận $10.", 10),
            new MoneyCard("Nhận từ quỹ cộng đồng $100.", 100),
            new MoneyCard("Nộp phí dịch vụ $25.", -25)
        ));
        communityChestDeck = new CardDeck(communityCards);
    }

    // =========================================================
    // 2. KHỞI TẠO 40 Ô BÀN CỜ
    // =========================================================
    private void initializeSquares() {
        // --- Hàng 1 (dưới): ô 0–9 ---
        squares[0]  = new SpecialSquare(0, "GO – Khởi Hành");
        squares[1]  = new PropertySquare(1,  "Đường Bắc Sơn",         60,  2,  50, ColorGroup.PURPLE);
        squares[2]  = new CommunityChestSquare(2, "Khí Vận", communityChestDeck);
        squares[3]  = new PropertySquare(3,  "Đường Đinh Tiên Hoàng",  60,  4,  50, ColorGroup.PURPLE);
        squares[4]  = new TaxSquare(4, "Thuế Thu Nhập", 200);
        squares[5]  = new RailroadSquare(5,  "Ga Hà Nội");
        squares[6]  = new PropertySquare(6,  "Đường Lý Thường Kiệt",  100,  6,  50, ColorGroup.LIGHT_BLUE);
        squares[7]  = new ChanceSquare(7, "Cơ Hội", chanceDeck);
        squares[8]  = new PropertySquare(8,  "Đường Hai Bà Trưng",    100,  6,  50, ColorGroup.LIGHT_BLUE);
        squares[9]  = new PropertySquare(9,  "Đường Nguyễn Huệ",      120,  8,  50, ColorGroup.LIGHT_BLUE);

        // --- Hàng 2 (trái): ô 10–19 ---
        squares[10] = new JailSquare(10, "Tù / Thăm Tù", false);
        squares[11] = new PropertySquare(11, "Đường Lê Lợi",           140, 10, 100, ColorGroup.PINK);
        squares[12] = new UtilitySquare(12, "Công Ty Điện Lực");
        squares[13] = new PropertySquare(13, "Đường Trần Hưng Đạo",    140, 10, 100, ColorGroup.PINK);
        squares[14] = new PropertySquare(14, "Đường Nguyễn Trãi",      160, 12, 100, ColorGroup.PINK);
        squares[15] = new RailroadSquare(15, "Ga Sài Gòn");
        squares[16] = new PropertySquare(16, "Đường Đồng Khởi",        180, 14, 100, ColorGroup.ORANGE);
        squares[17] = new CommunityChestSquare(17, "Khí Vận", communityChestDeck);
        squares[18] = new PropertySquare(18, "Đường Nam Kỳ Khởi Nghĩa",180, 14, 100, ColorGroup.ORANGE);
        squares[19] = new PropertySquare(19, "Đường Lê Duẩn",          200, 16, 100, ColorGroup.ORANGE);

        // --- Hàng 3 (trên): ô 20–29 ---
        squares[20] = new SpecialSquare(20, "Bãi Đỗ Xe Miễn Phí");
        squares[21] = new PropertySquare(21, "Đường Pasteur",           220, 18, 150, ColorGroup.RED);
        squares[22] = new ChanceSquare(22, "Cơ Hội", chanceDeck);
        squares[23] = new PropertySquare(23, "Đường Điện Biên Phủ",    220, 18, 150, ColorGroup.RED);
        squares[24] = new PropertySquare(24, "Đường Võ Văn Tần",       240, 20, 150, ColorGroup.RED);
        squares[25] = new RailroadSquare(25, "Ga Đà Nẵng");
        squares[26] = new PropertySquare(26, "Đường Hùng Vương",       260, 22, 150, ColorGroup.YELLOW);
        squares[27] = new PropertySquare(27, "Đường 3 Tháng 2",        260, 22, 150, ColorGroup.YELLOW);
        squares[28] = new UtilitySquare(28, "Công Ty Cấp Nước");
        squares[29] = new PropertySquare(29, "Đường Nguyễn Đình Chiểu",280, 24, 150, ColorGroup.YELLOW);

        // --- Hàng 4 (phải): ô 30–39 ---
        squares[30] = new JailSquare(30, "Vào Tù", true);
        squares[31] = new PropertySquare(31, "Đường Phan Đăng Lưu",    300, 26, 200, ColorGroup.GREEN);
        squares[32] = new PropertySquare(32, "Đường Phan Xích Long",    300, 26, 200, ColorGroup.GREEN);
        squares[33] = new CommunityChestSquare(33, "Khí Vận", communityChestDeck);
        squares[34] = new PropertySquare(34, "Đại Lộ Thống Nhất",      320, 28, 200, ColorGroup.GREEN);
        squares[35] = new RailroadSquare(35, "Ga Huế");
        squares[36] = new ChanceSquare(36, "Cơ Hội", chanceDeck);
        squares[37] = new PropertySquare(37, "Công Viên Hoàng Gia",    350, 35, 200, ColorGroup.DARK_BLUE);
        squares[38] = new TaxSquare(38, "Thuế Xa Xỉ", 100);
        squares[39] = new PropertySquare(39, "Dinh Thống Nhất",        400, 50, 200, ColorGroup.DARK_BLUE);
    }

    // =========================================================
    // 3. CÁC HÀM TIỆN ÍCH
    // =========================================================

    /** Lấy ô tại vị trí chỉ định. */
    public Square getSquare(int position) {
        return squares[position % SIZE];
    }

    /**
     * Cập nhật cờ colorGroupComplete cho toàn bộ PropertySquare trên bàn.
     * Nên gọi sau mỗi giao dịch mua đất.
     */
    public void updateColorGroups() {
        for (Square sq : squares) {
            if (sq instanceof PropertySquare) {
                PropertySquare prop = (PropertySquare) sq;
                ColorGroup group = prop.getColorGroup();
                if (group == null) continue; // Utility/Railroad không có nhóm màu
                Player owner = prop.getOwner();
                if (owner == null) {
                    prop.setColorGroupComplete(false);
                } else {
                    prop.setColorGroupComplete(hasColorGroup(owner, group));
                }
            }
        }
    }

    /**
     * Kiểm tra người chơi p có sở hữu toàn bộ ô trong nhóm màu group không.
     */
    public boolean hasColorGroup(Player p, ColorGroup group) {
        int required = 0, owned = 0;
        for (Square sq : squares) {
            if (sq instanceof PropertySquare) {
                PropertySquare prop = (PropertySquare) sq;
                if (prop.getColorGroup() == group) {
                    required++;
                    if (prop.getOwner() == p) owned++;
                }
            }
        }
        return required > 0 && owned == required;
    }

    /** In trạng thái toàn bộ các ô có chủ sở hữu (debug/info). */
    public void printOwnedProperties() {
        System.out.println("\n--- Tài Sản Hiện Tại ---");
        for (Square sq : squares) {
            if (sq instanceof PropertySquare) {
                PropertySquare prop = (PropertySquare) sq;
                if (prop.getOwner() != null) {
                    String level = prop.getHouseLevel() == 5 ? "🏨" :
                                   prop.getHouseLevel() > 0  ? "🏠×" + prop.getHouseLevel() : "";
                    System.out.printf("  Ô %2d: %-30s → %-15s %s%n",
                            prop.getPosition(), prop.getName(),
                            prop.getOwner().getName(), level);
                }
            }
        }
        System.out.println("------------------------");
    }
}
