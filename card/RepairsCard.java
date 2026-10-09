package ProjectOop.card;

import ProjectOop.player.Player;
import ProjectOop.square.PropertySquare;

/**
 * Thẻ Sửa Chữa – tính phí dựa trên số nhà & khách sạn người chơi đang có.
 *   Phí = (số_nhà × costPerHouse) + (số_khách_sạn × costPerHotel)
 * houseLevel 1 = Đất nền (0 nhà), 2 = Nhà phố (1 nhà), 3 = Chung cư (2 nhà), 4 = Khách sạn.
 */
public class RepairsCard extends Card {
    private int costPerHouse;
    private int costPerHotel;

    public RepairsCard(String description, int costPerHouse, int costPerHotel) {
        super(description);
        this.costPerHouse = costPerHouse;
        this.costPerHotel = costPerHotel;
    }

    @Override
    public void apply(Player p) {
        int totalCost = 0;
        for (PropertySquare prop : p.getOwnedProperties()) {
            int level = prop.getHouseLevel();
            if (level >= 4) {       // Khách sạn (Cấp 4)
                totalCost += costPerHotel;
            } else if (level > 1) { // Cấp 2: 1 nhà, Cấp 3: 2 nhà
                totalCost += (level - 1) * costPerHouse;
            }
        }
        System.out.println(p.getName() + " phải trả phí sửa chữa: $" + totalCost);
        if (!p.deductMoney(totalCost)) {
            p.deductMoney(p.getBalance());
            p.setBankrupt(true);
        }
    }
}
