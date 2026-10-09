package ProjectOop.square;

import ProjectOop.player.*;

public class PropertySquare extends Square {
    private static final int[] RENT_MULTIPLIERS = {0, 1, 5, 15, 52}; // 1: Đất nền, 2: Nhà phố, 3: Chung cư, 4: Khách sạn

    private int price;      // Giá mua ô đất
    private int baseRent;   // Tiền thuê gốc (chưa xây nhà)
    private int houseLevel; // 0: chưa có chủ, 1: đất nền, 2: nhà phố, 3: chung cư, 4: khách sạn (tối đa)
    private int houseCost;  // Giá nâng cấp mỗi cấp nhà/khách sạn
    private Player owner;   // Người sở hữu
    private boolean colorGroupComplete; // Cờ: chủ đã đủ bộ màu (Board.updateColorGroups() set)
    private ColorGroup colorGroup;      // Nhóm màu của ô đất (null nếu không phải đất màu)

    //1. Constructor đầy đủ (có ColorGroup)
    public PropertySquare(int position, String name, int price, int baseRent, int houseCost, ColorGroup colorGroup) {
        super(position, name);
        this.price = price;
        this.baseRent = baseRent;
        this.houseCost = houseCost;
        this.houseLevel = 0;
        this.owner = null;
        this.colorGroupComplete = false;
        this.colorGroup = colorGroup;
    }

    //  Constructor cũ (không có ColorGroup - dùng cho Utility, Railroad)
    public PropertySquare(int position, String name, int price, int baseRent, int houseCost) {
        this(position, name, price, baseRent, houseCost, null);
    }

    //2. Tính tiền thuê nhà
    public int getRent() {
        return getRentForLevel(this.houseLevel);
    }

    public int getRentForLevel(int level) {
        if (level <= 0 || level >= RENT_MULTIPLIERS.length) { return baseRent; }
        // Đất nền nhưng đã đủ bộ màu → tiền thuê gấp đôi
        if (level == 1 && colorGroupComplete) { return baseRent * 2; }
        return baseRent * RENT_MULTIPLIERS[level];
    }

    //3. Hiệu ứng xử lý khi có người đặt chân vào ô đất
    @Override
    public void applyEffect(Player p) {
        if (owner != null && p != owner) {
            int rent = getRent();
            System.out.println(p.getName() + " phải trả $" + rent + " tiền thuê cho " + owner.getName());
            if (p.deductMoney(rent)) {
                owner.addMoney(rent);
            } else {
                // Phá sản do nợ người chơi: set creditor trước khi setBankrupt
                owner.addMoney(p.getBalance());
                p.deductMoney(p.getBalance());
                p.setCreditor(owner); // ✓ Đánh dấu chủ nợ
                p.setBankrupt(true);
            }
        }
    }

    //4. Các hàm xử lý cho lựa chọn của người chơi

    // Kiểm tra có thể mua ô này không
    public boolean canBuy(Player p) {
        return owner == null && p.getBalance() >= price;
    }
    // Thực hiện mua đất (bắt đầu từ Cấp 1: Đất nền)
    public boolean buyProperty(Player p) {
        if (canBuy(p)) {
            p.deductMoney(price);
            this.owner = p;
            this.houseLevel = 1; // Bắt đầu từ Cấp 1: Đất nền
            p.addProperty(this);
            return true;
        }
        return false;
    }

    // Kiểm tra có thể nâng cấp (+1 cấp mỗi lần vào, tối đa 4 cấp)
    public boolean canUpgrade(Player p) {
        return owner == p && houseCost > 0 && houseLevel >= 1 && houseLevel < 4 && p.getBalance() >= houseCost;
    }
    // Thực hiện nâng cấp nhà/khách sạn (+1 cấp)
    public boolean upgrade(Player p) {
        if (canUpgrade(p)) {
            p.deductMoney(houseCost);
            houseLevel++;
            return true;
        }
        return false;
    }

    //5. GETTER và SETTER
    public Player getOwner() { return owner; }
    public int getHouseLevel() { return houseLevel; }
    public int getPrice() { return price; }
    public int getBaseRent() { return baseRent; }
    public int getHouseCost() { return houseCost; }
    public ColorGroup getColorGroup() { return colorGroup; }
    public void setOwner(Player owner) { this.owner = owner; }
    public void setHouseLevel(int houseLevel) { this.houseLevel = houseLevel; }
    public boolean isColorGroupComplete() { return colorGroupComplete; }
    public void setColorGroupComplete(boolean colorGroupComplete) {
        this.colorGroupComplete = colorGroupComplete;
    }

    /**
     * Trả ô đất về ngân hàng khi chủ sở hữu phá sản do nợ ngân hàng.
     */
    public void releaseProperty() {
        this.owner = null;
        this.houseLevel = 0;
        this.colorGroupComplete = false;
    }

    /**
     * Chuyển giao ô đất cho chủ nợ khi chủ sở hữu phá sản do nợ người chơi.
     * Nhà/khách sạn bị xóa (theo luật Monopoly khi chuyển giao tài sản phá sản).
     */
    public void transferPropertyTo(Player newOwner) {
        this.owner = newOwner;
        this.houseLevel = 1; // Giữ ở mức Đất nền (Cấp 1) cho chủ mới
        this.colorGroupComplete = false;
        newOwner.addProperty(this);
    }
}
