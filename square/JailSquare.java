package ProjectOop.square;

import ProjectOop.player.*;

public class JailSquare extends Square {
    private boolean isGoToJail; // true: ô 'Vào tù' (Go To Jail), false: ô 'Thăm tù' (Jail)

    public static final int BAIL_AMOUNT = 50;  // Phí bảo lãnh ra tù (có thể thay đổi)
    public static final int JAIL_POSITION = 10; // Vị trí cố định của ô tù trên bàn cờ 40 ô
                                                // Sẽ là 10 nếu ô khởi đầu là 0

    //1. Constructor
    public JailSquare(int position, String name, boolean isGoToJail) {
        super(position, name);
        this.isGoToJail = isGoToJail;
    }

    //2. Hiệu ứng xử lý khi có người đặt chân vào ô đất
    @Override
    public void applyEffect(Player p) {
        // Rơi trúng ô 'Vào tù' (ô 30) -> Bắt buộc bị áp giải đến ô số 10 và đổi trạng thái
        if(isGoToJail) {
            p.setPosition(JAIL_POSITION); // ✅ Sửa: dùng setter thay vì truy cập field trực tiếp
            p.setInJail(true);
            p.setTurnsInJail(0);
        }
        // Nếu rơi vào ô số 10 bình thường: Khách thăm, không có hiệu ứng gì
    }

    //3. Các hàm xử lý cho lựa chọn của người chơi
    // Logic cho tương tác cài sau khi có UI

    // Lựa chọn 1: Nộp tiền bảo lãnh để ra tù ngay lập tức
    public boolean payBail(Player p) {
        if(p.isInJail() && p.getBalance() >= BAIL_AMOUNT) {
            p.deductMoney(BAIL_AMOUNT);
            p.setInJail(false);
            p.setTurnsInJail(0);
            return true;
        }
        return false;
    }

    // Lựa chọn 2: Dùng thẻ miễn phí ra tù (nếu có)
    public boolean useTicketToFree(Player p) {
        if(p.isInJail() && p.useGetOutOfJailTicket()) {
            p.setInJail(false);
            p.setTurnsInJail(0);
            return true;
        }
        return false;
    }

    //4. GETTER và SETTER
    public boolean isGoToJail() {
        return isGoToJail;
    }
}
