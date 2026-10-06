package ProjectOop.square;

import ProjectOop.player.*;

public class UtilitySquare extends PropertySquare {
    public static final int UTILITY_PRICE = 150; // Giá mua của công ty tiện ích
    private static final int ONE_UTILITY_MULTIPLIER = 4; // Hệ số nhân tiền thuê khi chủ sở hữu có 1 công ty tiện ích
    private static final int BOTH_UTILITIES_MULTIPLIER = 10; // Hệ số nhân tiền thuê khi chủ sở hữu có cả 2 công ty tiện ích

    public UtilitySquare(int position, String name, int price) {
        super(position, name, price, 0, 0);
    }

    public UtilitySquare(int position, String name) {
        this(position, name, UTILITY_PRICE);
    }

    // 1. Hàm tính tiền thuê dựa trên số lượng công ty tiện ích mà chủ sở hữu sở hữu
    // ✅ Sửa: bỏ @Override vì đây là overload (khác signature) chứ không phải override
    public int getRent(int diceRoll) {
        int utilityCount = 0;
        Player owner = getOwner();
        if (owner != null) {
            for (PropertySquare square : owner.getOwnedProperties()) {
                // Duyệt các ô Property đã sở hữu xem có phải là UtilitySquare không
                if (square instanceof UtilitySquare) {
                    utilityCount++;
                }
            }
        }
        // Phép toán 3 ngôi xác định xem hệ số cần dùng
        int multiplier = utilityCount == 2
                ? BOTH_UTILITIES_MULTIPLIER
                : ONE_UTILITY_MULTIPLIER;
        return diceRoll * multiplier;
    }

    // 2. Hiệu ứng xử lý khi có người chơi đặt chân vào ô tiện ích
    @Override
    public void applyEffect(Player p) {
        Player owner = getOwner();
        if (owner != null && p != owner) {
            int diceRoll = p.getLastDiceRoll();
            if (diceRoll < 2 || diceRoll > 12) {
                return;
            }
            int rent = getRent(diceRoll);
            if (p.deductMoney(rent)) {
                owner.addMoney(rent);
            } else {
                owner.addMoney(p.getBalance());
                p.deductMoney(p.getBalance());
                p.setCreditor(owner); // Đánh dấu chủ nợ
                p.setBankrupt(true);
            }
        }
    }

    public boolean buyUtility(Player p) {
        return buyProperty(p);
    }

    // 3. UtilitySquare không có nâng cấp
    @Override
    public boolean canUpgrade(Player p) {
        return false;
    }

    @Override
    public boolean upgrade(Player p) {
        return false;
    }
}
