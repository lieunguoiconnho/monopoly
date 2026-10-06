package ProjectOop.square;

import ProjectOop.card.Card;
import ProjectOop.card.CardDeck;
import ProjectOop.player.*;

/**
 * Ô Khí Vận (Community Chest) – người chơi rút 1 thẻ ngẫu nhiên từ bộ bài Khí Vận.
 * Các vị trí: 2, 17, 33.
 */
public class CommunityChestSquare extends Square {
    private CardDeck deck;
    private Card lastDrawnCard;

    public CommunityChestSquare(int position, String name, CardDeck deck) {
        super(position, name);
        this.deck = deck;
    }

    public Card getLastDrawnCard() {
        return lastDrawnCard;
    }

    @Override
    public void applyEffect(Player p) {
        System.out.println(p.getName() + " dừng tại ô Khí Vận!");
        lastDrawnCard = deck.draw();
        System.out.println("  >> " + lastDrawnCard.getDescription());
        lastDrawnCard.apply(p);
    }
}
