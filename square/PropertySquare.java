package ProjectOop.square;

import ProjectOop.player.*;

public class PropertySquare extends Square {
    private static final int[] RENT_MULTIPLIERS = {1, 5, 15, 36, 44, 52}; // bảng giá nhân giá theo cấp (nên cân bằng lại)

    private int price;      // Giá mua ô đất
    private int baseRent;   // Tiền thuê gốc (chưa xây nhà)
    private int houseLevel; // 0: đất trống, 1-4: số nhà, 5: khách sạn
    private int houseCost;  // Giá nâng cấp mỗi cấp nhà/khách sạn
    private Player owner;   // Người sở hữu
    private boolean colorGroupComplete; // Cờ: chủ đã đủ bộ màu (Board.updateColorGroups() set)
    private ColorGroup colorGroup;      // ✅ Thêm: nhóm màu của ô đất (null nếu không phải đất màu)

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
        if (houseLevel < 0 || houseLevel >= RENT_MULTIPLIERS.length) { return baseRent; }
        // ✅ Đất trống nhưng đã đủ bộ màu → tiền thuê gấp đôi
        if (houseLevel == 0 && colorGroupComplete) { return baseRent * 2; }
        return baseRent * RENT_MULTIPLIERS[houseLevel];
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
    // Thực hiện mua đất
    public boolean buyProperty(Player p) {
        if (canBuy(p)) {
            p.deductMoney(price);
            this.owner = p;
            this.houseLevel = 0; // Sau khi mua là đất trống (houseLevel=0), chỉ khi xây nhà mới tăng cấp
            p.addProperty(this);
            return true;
        }
        return false;
    }

    // Kiểm tra có thể nâng cấp (chỉ được nâng cấp khi đã đủ bộ màu theo RULES.md)
    public boolean canUpgrade(Player p) {
        return owner == p && houseCost > 0 && colorGroupComplete && houseLevel < 5 && p.getBalance() >= houseCost;
    }
    // Thực hiện nâng cấp nhà/khách sạn (nâng 1 cấp 1 lần)
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
        this.houseLevel = 0; // Xóa nhà theo luật
        this.colorGroupComplete = false;
        newOwner.addProperty(this);
    }
}
