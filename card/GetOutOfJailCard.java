package ProjectOop.card;

import ProjectOop.player.Player;

/**
 * Thẻ Ra Tù Miễn Phí – thêm 1 thẻ ra tù vào túi người chơi.
 * Người chơi có thể giữ và dùng bất cứ lúc nào khi đang ở tù.
 */
public class GetOutOfJailCard extends Card {

    public GetOutOfJailCard(String description) {
        super(description);
    }

    @Override
    public void apply(Player p) {
        p.addGetOutOfJailTicket();
        System.out.println(p.getName() + " nhận được thẻ Ra Tù Miễn Phí!");
    }
}
