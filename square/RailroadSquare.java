package ProjectOop.square;

import ProjectOop.player.*;

/**
 * Ô Nhà Ga (Railroad) – 4 ô tại vị trí 5, 15, 25, 35.
 * Tiền thuê tăng theo số ga mà chủ sở hữu đang nắm giữ:
 *   1 ga → $25 | 2 ga → $50 | 3 ga → $100 | 4 ga → $200
 */
public class RailroadSquare extends PropertySquare {
    public static final int RAILROAD_PRICE = 200;
    // Index = số ga đang sở hữu (0 không dùng, 1→$25, 2→$50, 3→$100, 4→$200)
    private static final int[] RENT_BY_COUNT = {0, 25, 50, 100, 200};

    public RailroadSquare(int position, String name, int price) {
        super(position, name, price, 25, 0); // baseRent=25 (1 ga), houseCost=0
    }

    public RailroadSquare(int position, String name) {
        this(position, name, RAILROAD_PRICE);
    }

    // Tính tiền thuê theo số lượng ga chủ sở hữu đang nắm
    @Override
    public int getRent() {
        Player owner = getOwner();
        if (owner == null) return 0;
        int count = 0;
        for (PropertySquare sq : owner.getOwnedProperties()) {
            if (sq instanceof RailroadSquare) count++;
        }
        count = Math.max(1, Math.min(count, 4)); // clamp [1, 4]
        return RENT_BY_COUNT[count];
    }

    @Override
    public void applyEffect(Player p) {
        Player owner = getOwner();
        if (owner != null && p != owner) {
            int rent = getRent();
            System.out.println(p.getName() + " phải trả $" + rent + " tiền thuê ga cho " + owner.getName());
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

    // Railroad không có nâng cấp nhà
    @Override public boolean canUpgrade(Player p) { return false; }
    @Override public boolean upgrade(Player p) { return false; }

    public boolean buyRailroad(Player p) { return buyProperty(p); }
}
