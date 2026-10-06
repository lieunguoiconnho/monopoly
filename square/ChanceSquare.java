package ProjectOop.square;

import ProjectOop.card.Card;
import ProjectOop.card.CardDeck;
import ProjectOop.player.*;

/**
 * Ô Cơ Hội (Chance) – người chơi rút 1 thẻ ngẫu nhiên từ bộ bài Cơ Hội.
 * Các vị trí: 7, 22, 36.
 */
public class ChanceSquare extends Square {
    private CardDeck deck;
    private Card lastDrawnCard;

    public ChanceSquare(int position, String name, CardDeck deck) {
        super(position, name);
        this.deck = deck;
    }

    public Card getLastDrawnCard() {
        return lastDrawnCard;
    }

    @Override
    public void applyEffect(Player p) {
        System.out.println(p.getName() + " dừng tại ô Cơ Hội!");
        lastDrawnCard = deck.draw();
        System.out.println("  >> " + lastDrawnCard.getDescription());
        lastDrawnCard.apply(p);
    }
}