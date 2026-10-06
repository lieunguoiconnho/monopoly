package ProjectOop.square;

import ProjectOop.player.*;

/**
 * Ô Đặc Biệt – dùng cho:
 *   Ô 0: Khởi Hành (GO) – tiền $200 khi đi qua được xử lý trong Player.move()
 *   Ô 20: Bãi Đỗ Xe Miễn Phí – không có hiệu ứng gì
 */
public class SpecialSquare extends Square {

    public SpecialSquare(int position, String name) {
        super(position, name);
    }

    @Override
    public void applyEffect(Player p) {
        // Không có hiệu ứng khi dừng tại đây
        System.out.println(p.getName() + " đứng tại " + name + ".");
    }
}
